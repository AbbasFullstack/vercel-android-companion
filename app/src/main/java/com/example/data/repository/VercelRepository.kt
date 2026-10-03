package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.VercelApiService
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class VercelRepository(
    private val tokenDao: TokenDao,
    private val cachedProjectDao: CachedProjectDao,
    private val api: VercelApiService = ApiClient.apiService
) {

    val activeTokenFlow: Flow<TokenEntity?> = tokenDao.getActiveToken()
    val allTokensFlow: Flow<List<TokenEntity>> = tokenDao.getAllTokens()

    suspend fun initializeToken() {
        withContext(Dispatchers.IO) {
            val active = tokenDao.getActiveTokenSync()
            ApiClient.tokenHolder.token = active?.token
        }
    }

    suspend fun validateAndSaveToken(rawToken: String): Result<VercelUser> = withContext(Dispatchers.IO) {
        val cleanToken = rawToken.trim()
        if (cleanToken.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Token cannot be empty"))
        }

        try {
            ApiClient.tokenHolder.token = cleanToken
            val response = api.getUser()
            val user = response.user
            val entity = TokenEntity(
                token = cleanToken,
                userId = user.id,
                username = user.username,
                name = user.name ?: user.username,
                email = user.email,
                avatar = user.avatar,
                isActive = true
            )
            tokenDao.setActiveToken(entity)
            Result.success(user)
        } catch (e: Exception) {
            ApiClient.tokenHolder.token = tokenDao.getActiveTokenSync()?.token
            Result.failure(e)
        }
    }

    suspend fun switchActiveToken(token: String) = withContext(Dispatchers.IO) {
        tokenDao.deactivateAll()
        tokenDao.activateToken(token)
        ApiClient.tokenHolder.token = token
    }

    suspend fun removeToken(token: String) = withContext(Dispatchers.IO) {
        tokenDao.deleteToken(token)
        val remainingActive = tokenDao.getActiveTokenSync()
        ApiClient.tokenHolder.token = remainingActive?.token
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        tokenDao.clearAll()
        cachedProjectDao.clearProjects(null)
        ApiClient.tokenHolder.token = null
    }

    suspend fun getTeams(): Result<List<VercelTeam>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getTeams()
            Result.success(resp.teams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCachedProjects(teamId: String?): List<VercelProject> = withContext(Dispatchers.IO) {
        cachedProjectDao.getProjectsSync(teamId).map { entity ->
            val targetsMap = if (!entity.productionUrl.isNullOrBlank()) {
                mapOf("production" to VercelTarget(id = entity.id, url = entity.productionUrl, readyState = entity.productionState ?: "READY", createdAt = entity.updatedAt))
            } else null
            val gitLink = if (!entity.repoName.isNullOrBlank()) VercelGitLink(type = "github", repo = entity.repoName, org = null) else null
            VercelProject(
                id = entity.id,
                name = entity.name,
                framework = entity.framework,
                updatedAt = entity.updatedAt,
                createdAt = entity.updatedAt,
                targets = targetsMap,
                link = gitLink
            )
        }
    }

    suspend fun getProjects(teamId: String?, search: String? = null): Result<List<VercelProject>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.getProjects(teamId = teamId, search = search)
                // Cache projects in Room for fast offline startup
                val entities = resp.projects.map { p ->
                    val prodTarget = p.targets?.get("production")
                    CachedProjectEntity(
                        id = p.id,
                        name = p.name,
                        framework = p.framework,
                        updatedAt = p.updatedAt,
                        productionUrl = prodTarget?.url,
                        productionState = prodTarget?.readyState,
                        repoName = p.link?.repo,
                        teamId = teamId
                    )
                }
                cachedProjectDao.clearProjects(teamId)
                cachedProjectDao.insertProjects(entities)

                Result.success(resp.projects)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getProject(idOrName: String, teamId: String?): Result<VercelProject> =
        withContext(Dispatchers.IO) {
            try {
                val project = api.getProject(idOrName = idOrName, teamId = teamId)
                Result.success(project)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun parseErrorMessage(e: Exception): String {
        if (e is retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                try {
                    val json = JSONObject(errorBody)
                    val errObj = json.optJSONObject("error")
                    if (errObj != null) {
                        val msg = errObj.optString("message")
                        if (msg.isNotBlank()) return msg
                    }
                    val msg = json.optString("message")
                    if (msg.isNotBlank()) return msg
                } catch (_: Exception) {}
                return errorBody
            }
        }
        return e.message ?: "An unknown error occurred"
    }

    suspend fun getGitHubUserRepos(username: String): Result<List<GitHubRepo>> = withContext(Dispatchers.IO) {
        try {
            val cleanUser = username.trim().removePrefix("@")
            val repos = ApiClient.gitHubService.getUserRepos(cleanUser)
            Result.success(repos)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e)))
        }
    }

    suspend fun createProject(
        name: String,
        framework: String?,
        gitRepo: String? = null,
        rootDirectory: String? = null,
        buildCommand: String? = null,
        teamId: String?
    ): Result<VercelProject> = withContext(Dispatchers.IO) {
        try {
            val gitPayload = if (!gitRepo.isNullOrBlank()) {
                val cleanRepo = gitRepo.trim()
                    .removePrefix("https://github.com/")
                    .removePrefix("http://github.com/")
                    .removePrefix("github.com/")
                    .removeSuffix(".git")
                GitRepoPayload(type = "github", repo = cleanRepo)
            } else null

            val project = api.createProject(
                request = CreateProjectRequest(
                    name = name,
                    framework = framework,
                    gitRepository = gitPayload,
                    rootDirectory = if (rootDirectory.isNullOrBlank()) null else rootDirectory.trim(),
                    buildCommand = if (buildCommand.isNullOrBlank()) null else buildCommand.trim()
                ),
                teamId = teamId
            )
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e)))
        }
    }

    suspend fun deployGitRepo(
        name: String,
        projectName: String,
        gitRepo: String,
        branch: String = "main",
        teamId: String?
    ): Result<VercelDeployment> = withContext(Dispatchers.IO) {
        try {
            val cleanRepo = gitRepo.trim()
                .removePrefix("https://github.com/")
                .removePrefix("http://github.com/")
                .removePrefix("github.com/")
                .removeSuffix(".git")

            val deployment = api.createDeployment(
                request = CreateDeploymentRequest(
                    name = name,
                    project = projectName,
                    gitSource = GitSourcePayload(type = "github", repo = cleanRepo, ref = branch),
                    target = "production"
                ),
                teamId = teamId
            )
            Result.success(deployment)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e)))
        }
    }

    suspend fun getDeployments(
        projectId: String? = null,
        teamId: String? = null,
        limit: Int = 30,
        state: String? = null
    ): Result<List<VercelDeployment>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getDeployments(
                projectId = projectId,
                teamId = teamId,
                limit = limit,
                state = state
            )
            Result.success(resp.deployments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeployment(id: String, teamId: String?): Result<VercelDeployment> =
        withContext(Dispatchers.IO) {
            try {
                val dpl = api.getDeployment(id = id, teamId = teamId)
                Result.success(dpl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun redeploy(
        name: String,
        deploymentId: String,
        teamId: String?,
        forceNew: Boolean = false
    ): Result<VercelDeployment> = withContext(Dispatchers.IO) {
        try {
            val dpl = api.redeploy(
                request = RedeployRequest(name = name, deploymentId = deploymentId),
                teamId = teamId,
                forceNew = if (forceNew) 1 else null
            )
            Result.success(dpl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelDeployment(id: String, teamId: String?): Result<VercelDeployment> =
        withContext(Dispatchers.IO) {
            try {
                val dpl = api.cancelDeployment(id = id, teamId = teamId)
                Result.success(dpl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getDeploymentLogs(id: String, teamId: String?): Result<List<DeploymentLogItem>> =
        withContext(Dispatchers.IO) {
            try {
                val body = api.getDeploymentEvents(id = id, teamId = teamId)
                val rawString = body.string()
                val logList = mutableListOf<DeploymentLogItem>()

                // Could be NDJSON (line-separated JSON) or a JSON array or plaintext lines
                val trimmed = rawString.trim()
                if (trimmed.startsWith("[")) {
                    val array = JSONArray(trimmed)
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i)
                        if (obj != null) {
                            val text = obj.optString("text", obj.optString("payload", ""))
                            val type = obj.optString("type", "stdout")
                            val created = obj.optLong("created", obj.optLong("date", System.currentTimeMillis()))
                            val eventId = obj.optString("id", i.toString())
                            logList.add(DeploymentLogItem(eventId, text, type, created))
                        }
                    }
                } else {
                    val lines = rawString.lines()
                    lines.forEachIndexed { index, line ->
                        if (line.isNotBlank()) {
                            try {
                                val obj = JSONObject(line)
                                val text = obj.optString("text", obj.optString("payload", line))
                                val type = obj.optString("type", "stdout")
                                val created = obj.optLong("created", obj.optLong("date", System.currentTimeMillis()))
                                val eventId = obj.optString("id", index.toString())
                                logList.add(DeploymentLogItem(eventId, text, type, created))
                            } catch (_: Exception) {
                                logList.add(
                                    DeploymentLogItem(
                                        id = index.toString(),
                                        text = line,
                                        type = "stdout",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                }
                Result.success(logList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getProjectDomains(idOrName: String, teamId: String?): Result<List<VercelDomain>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.getProjectDomains(idOrName = idOrName, teamId = teamId)
                Result.success(resp.domains)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun addProjectDomain(idOrName: String, domain: String, teamId: String?): Result<VercelDomain> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.addProjectDomain(idOrName, AddDomainRequest(domain), teamId)
                Result.success(res)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getProjectEnv(idOrName: String, teamId: String?): Result<List<VercelEnvVar>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.getProjectEnv(idOrName = idOrName, teamId = teamId)
                Result.success(resp.envs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun addProjectEnv(
        idOrName: String,
        key: String,
        value: String,
        type: String,
        target: List<String>,
        teamId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.addProjectEnv(
                idOrName = idOrName,
                request = AddEnvVarRequest(key = key, value = value, type = type, target = target),
                teamId = teamId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
