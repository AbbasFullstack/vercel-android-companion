package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TokenEntity
import com.example.data.model.*
import com.example.data.repository.VercelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Auth : Screen
    data object Dashboard : Screen
    data class ProjectDetail(val project: VercelProject) : Screen
    data class DeploymentDetail(val deployment: VercelDeployment) : Screen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = VercelRepository(db.tokenDao(), db.cachedProjectDao())

    val activeToken: StateFlow<TokenEntity?> = repository.activeTokenFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTokens: StateFlow<List<TokenEntity>> = repository.allTokensFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Auth)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _verifiedUser = MutableStateFlow<VercelUser?>(null)
    val verifiedUser: StateFlow<VercelUser?> = _verifiedUser.asStateFlow()

    private val _teams = MutableStateFlow<List<VercelTeam>>(emptyList())
    val teams: StateFlow<List<VercelTeam>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow<VercelTeam?>(null)
    val selectedTeam: StateFlow<VercelTeam?> = _selectedTeam.asStateFlow()

    private val _projects = MutableStateFlow<List<VercelProject>>(emptyList())
    val projects: StateFlow<List<VercelProject>> = _projects.asStateFlow()

    private val _deployments = MutableStateFlow<List<VercelDeployment>>(emptyList())
    val deployments: StateFlow<List<VercelDeployment>> = _deployments.asStateFlow()

    private val _projectDeployments = MutableStateFlow<List<VercelDeployment>>(emptyList())
    val projectDeployments: StateFlow<List<VercelDeployment>> = _projectDeployments.asStateFlow()

    private val _projectDomains = MutableStateFlow<List<VercelDomain>>(emptyList())
    val projectDomains: StateFlow<List<VercelDomain>> = _projectDomains.asStateFlow()

    private val _projectEnvVars = MutableStateFlow<List<VercelEnvVar>>(emptyList())
    val projectEnvVars: StateFlow<List<VercelEnvVar>> = _projectEnvVars.asStateFlow()

    private val _deploymentLogs = MutableStateFlow<List<DeploymentLogItem>>(emptyList())
    val deploymentLogs: StateFlow<List<DeploymentLogItem>> = _deploymentLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLogsLoading = MutableStateFlow(false)
    val isLogsLoading: StateFlow<Boolean> = _isLogsLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _projectToRedeploy = MutableStateFlow<VercelProject?>(null)
    val projectToRedeploy: StateFlow<VercelProject?> = _projectToRedeploy.asStateFlow()

    private val _isCreateProjectOpen = MutableStateFlow(false)
    val isCreateProjectOpen: StateFlow<Boolean> = _isCreateProjectOpen.asStateFlow()

    private val _gitHubRepos = MutableStateFlow<List<GitHubRepo>>(emptyList())
    val gitHubRepos: StateFlow<List<GitHubRepo>> = _gitHubRepos.asStateFlow()

    private val _isFetchingRepos = MutableStateFlow(false)
    val isFetchingRepos: StateFlow<Boolean> = _isFetchingRepos.asStateFlow()

    private val _gitHubError = MutableStateFlow<String?>(null)
    val gitHubError: StateFlow<String?> = _gitHubError.asStateFlow()

    fun fetchGitHubRepos(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            _isFetchingRepos.value = true
            _gitHubError.value = null
            val res = repository.getGitHubUserRepos(username.trim())
            _isFetchingRepos.value = false
            res.onSuccess { repos ->
                _gitHubRepos.value = repos
            }.onFailure { e ->
                _gitHubError.value = "GitHub repos: ${e.message}"
            }
        }
    }

    private val _connectedGitHubUser = MutableStateFlow("AbbasFullstack")
    val connectedGitHubUser: StateFlow<String> = _connectedGitHubUser.asStateFlow()

    fun updateConnectedGitHubUser(username: String) {
        _connectedGitHubUser.value = username.trim()
        fetchGitHubRepos(username)
    }

    fun oneClickImport(repo: GitHubRepo) {
        val framework = detectFramework(repo)
        val projName = repo.name.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
        createProject(
            name = projName,
            framework = framework,
            gitRepo = repo.fullName,
            rootDirectory = null,
            branch = repo.defaultBranch
        )
    }

    private fun detectFramework(repo: GitHubRepo): String? {
        val lowerName = repo.name.lowercase()
        val lowerDesc = repo.description?.lowercase() ?: ""
        return when {
            lowerName.contains("next") || lowerDesc.contains("next") -> "nextjs"
            lowerName.contains("vite") || lowerDesc.contains("vite") -> "vite"
            lowerName.contains("astro") || lowerDesc.contains("astro") -> "astro"
            lowerName.contains("remix") || lowerDesc.contains("remix") -> "remix"
            lowerName.contains("svelte") || lowerDesc.contains("svelte") -> "sveltekit"
            lowerName.contains("nuxt") || lowerDesc.contains("nuxt") -> "nuxtjs"
            lowerName.contains("react") || lowerDesc.contains("react") -> "create-react-app"
            repo.language.equals("TypeScript", ignoreCase = true) || repo.language.equals("JavaScript", ignoreCase = true) -> "nextjs"
            else -> null
        }
    }

    init {
        viewModelScope.launch {
            fetchGitHubRepos(_connectedGitHubUser.value)
            repository.initializeToken()
            activeToken.collect { token ->
                if (token != null) {
                    if (_currentScreen.value is Screen.Auth && _verifiedUser.value == null) {
                        _currentScreen.value = Screen.Dashboard
                    }
                    loadTeams()
                    refreshDashboard()
                } else {
                    _currentScreen.value = Screen.Auth
                }
            }
        }
    }

    fun connectToken(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.validateAndSaveToken(token)
            _isLoading.value = false
            result.onSuccess { user ->
                _verifiedUser.value = user
                loadTeams()
                refreshDashboard()
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Authentication failed. Please verify your token."
            }
        }
    }

    fun proceedToDashboard() {
        _currentScreen.value = Screen.Dashboard
        _verifiedUser.value = null
    }

    fun selectSavedToken(token: TokenEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.switchActiveToken(token.token)
            _selectedTeam.value = null
            _isLoading.value = false
            _currentScreen.value = Screen.Dashboard
            loadTeams()
            refreshDashboard()
        }
    }

    fun selectPersonal() {
        _selectedTeam.value = null
        refreshDashboard()
    }

    fun selectTeam(team: VercelTeam) {
        _selectedTeam.value = team
        refreshDashboard()
    }

    fun loadTeams() {
        viewModelScope.launch {
            val res = repository.getTeams()
            res.onSuccess {
                _teams.value = it
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val teamId = _selectedTeam.value?.id

            // Step 1: Instant cache load (0ms latency, zero screen flicker)
            val cached = repository.getCachedProjects(teamId)
            if (cached.isNotEmpty()) {
                _projects.value = cached
            }

            // Step 2: Background refresh with subtle top loading bar
            _isLoading.value = true
            _errorMessage.value = null

            val projectsRes = repository.getProjects(teamId = teamId)
            projectsRes.onSuccess {
                _projects.value = it
            }.onFailure { e ->
                if (_projects.value.isEmpty()) {
                    _errorMessage.value = "Failed to load projects: ${e.message}"
                }
            }

            val dplRes = repository.getDeployments(teamId = teamId)
            dplRes.onSuccess {
                _deployments.value = it
            }

            _isLoading.value = false
        }
    }

    fun openProject(project: VercelProject) {
        _currentScreen.value = Screen.ProjectDetail(project)
        loadProjectDetails(project)
    }

    fun loadProjectDetails(project: VercelProject) {
        viewModelScope.launch {
            _isLoading.value = true
            val teamId = _selectedTeam.value?.id

            // Load deployments for project
            val dplRes = repository.getDeployments(projectId = project.id, teamId = teamId)
            dplRes.onSuccess { _projectDeployments.value = it }

            // Load domains for project
            val domRes = repository.getProjectDomains(idOrName = project.id, teamId = teamId)
            domRes.onSuccess { _projectDomains.value = it }

            // Load env vars
            val envRes = repository.getProjectEnv(idOrName = project.id, teamId = teamId)
            envRes.onSuccess { _projectEnvVars.value = it }

            _isLoading.value = false
        }
    }

    fun openDeployment(deployment: VercelDeployment) {
        _currentScreen.value = Screen.DeploymentDetail(deployment)
        loadDeploymentLogs(deployment.uid)
    }

    fun loadDeploymentLogs(deploymentId: String) {
        viewModelScope.launch {
            _isLogsLoading.value = true
            val teamId = _selectedTeam.value?.id
            val res = repository.getDeploymentLogs(id = deploymentId, teamId = teamId)
            res.onSuccess {
                _deploymentLogs.value = it
            }
            _isLogsLoading.value = false
        }
    }

    fun openRedeployDialog(project: VercelProject) {
        _projectToRedeploy.value = project
    }

    fun closeRedeployDialog() {
        _projectToRedeploy.value = null
    }

    fun triggerRedeploy(forceNew: Boolean) {
        val proj = _projectToRedeploy.value ?: return
        val prodDplId = proj.targets?.get("production")?.id ?: return
        val teamId = _selectedTeam.value?.id

        viewModelScope.launch {
            closeRedeployDialog()
            _isLoading.value = true
            val res = repository.redeploy(
                name = proj.name,
                deploymentId = prodDplId,
                teamId = teamId,
                forceNew = forceNew
            )
            res.onSuccess { newDpl ->
                refreshDashboard()
                _currentScreen.value = Screen.DeploymentDetail(newDpl)
                loadDeploymentLogs(newDpl.uid)
            }.onFailure { e ->
                _errorMessage.value = "Redeploy failed: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun cancelDeployment(deployment: VercelDeployment) {
        viewModelScope.launch {
            _isLoading.value = true
            val teamId = _selectedTeam.value?.id
            val res = repository.cancelDeployment(id = deployment.uid, teamId = teamId)
            res.onSuccess { updated ->
                _currentScreen.value = Screen.DeploymentDetail(updated)
                refreshDashboard()
            }
            _isLoading.value = false
        }
    }

    fun openCreateProject() {
        _isCreateProjectOpen.value = true
    }

    fun closeCreateProject() {
        _isCreateProjectOpen.value = false
    }

    fun createProject(
        name: String,
        framework: String?,
        gitRepo: String? = null,
        rootDirectory: String? = null,
        branch: String = "main"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val teamId = _selectedTeam.value?.id
            val res = repository.createProject(
                name = name,
                framework = framework,
                gitRepo = gitRepo,
                rootDirectory = rootDirectory,
                teamId = teamId
            )
            _isLoading.value = false
            res.onSuccess { createdProj ->
                closeCreateProject()
                refreshDashboard()
                if (!gitRepo.isNullOrBlank()) {
                    viewModelScope.launch {
                        _isLoading.value = true
                        val deployRes = repository.deployGitRepo(
                            name = name,
                            projectName = createdProj.name,
                            gitRepo = gitRepo,
                            branch = branch,
                            teamId = teamId
                        )
                        _isLoading.value = false
                        deployRes.onSuccess { dpl ->
                            refreshDashboard()
                            _currentScreen.value = Screen.DeploymentDetail(dpl)
                            loadDeploymentLogs(dpl.uid)
                        }.onFailure { err ->
                            _errorMessage.value = "Project created! Deployment info: ${err.message}"
                            openProject(createdProj)
                            refreshDashboard()
                        }
                    }
                } else {
                    openProject(createdProj)
                }
            }.onFailure { e ->
                _errorMessage.value = "Failed to create project: ${e.message}"
            }
        }
    }

    fun addDomain(project: VercelProject, domain: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val teamId = _selectedTeam.value?.id
            val res = repository.addProjectDomain(idOrName = project.id, domain = domain, teamId = teamId)
            res.onSuccess {
                loadProjectDetails(project)
            }.onFailure { e ->
                _errorMessage.value = "Failed to add domain: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun addEnvVar(project: VercelProject, key: String, value: String, targets: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            val teamId = _selectedTeam.value?.id
            val res = repository.addProjectEnv(
                idOrName = project.id,
                key = key,
                value = value,
                type = "plain",
                target = targets,
                teamId = teamId
            )
            res.onSuccess {
                loadProjectDetails(project)
            }.onFailure { e ->
                _errorMessage.value = "Failed to add environment variable: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.logout()
            _selectedTeam.value = null
            _projects.value = emptyList()
            _deployments.value = emptyList()
            _verifiedUser.value = null
            _currentScreen.value = Screen.Auth
        }
    }

    fun navigateBack() {
        when (_currentScreen.value) {
            is Screen.DeploymentDetail -> {
                _currentScreen.value = Screen.Dashboard
            }
            is Screen.ProjectDetail -> {
                _currentScreen.value = Screen.Dashboard
            }
            else -> {
                // At root
            }
        }
    }
}
