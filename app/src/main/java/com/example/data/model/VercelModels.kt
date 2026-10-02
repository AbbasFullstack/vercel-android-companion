package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VercelUserResponse(
    @Json(name = "user") val user: VercelUser
)

@JsonClass(generateAdapter = true)
data class VercelUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "username") val username: String,
    @Json(name = "avatar") val avatar: String?
)

@JsonClass(generateAdapter = true)
data class VercelTeamsResponse(
    @Json(name = "teams") val teams: List<VercelTeam> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VercelTeam(
    @Json(name = "id") val id: String,
    @Json(name = "slug") val slug: String,
    @Json(name = "name") val name: String,
    @Json(name = "avatar") val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class VercelProjectsResponse(
    @Json(name = "projects") val projects: List<VercelProject> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VercelProject(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "framework") val framework: String? = null,
    @Json(name = "updatedAt") val updatedAt: Long = 0L,
    @Json(name = "createdAt") val createdAt: Long = 0L,
    @Json(name = "targets") val targets: Map<String, VercelTarget>? = null,
    @Json(name = "link") val link: VercelGitLink? = null
)

@JsonClass(generateAdapter = true)
data class VercelTarget(
    @Json(name = "id") val id: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "readyState") val readyState: String? = null,
    @Json(name = "createdAt") val createdAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class VercelGitLink(
    @Json(name = "type") val type: String? = null,
    @Json(name = "repo") val repo: String? = null,
    @Json(name = "org") val org: String? = null
)

@JsonClass(generateAdapter = true)
data class VercelDeploymentsResponse(
    @Json(name = "deployments") val deployments: List<VercelDeployment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VercelDeployment(
    @Json(name = "uid") val uid: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String,
    @Json(name = "state") val state: String, // READY, ERROR, BUILDING, CANCELED, QUEUED, INITIALIZING
    @Json(name = "created") val created: Long = 0L,
    @Json(name = "creator") val creator: VercelCreator? = null,
    @Json(name = "meta") val meta: DeploymentMeta? = null,
    @Json(name = "target") val target: String? = null,
    @Json(name = "inspectorUrl") val inspectorUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class VercelCreator(
    @Json(name = "uid") val uid: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "avatar") val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class DeploymentMeta(
    @Json(name = "githubCommitMessage") val githubCommitMessage: String? = null,
    @Json(name = "githubCommitRef") val githubCommitRef: String? = null,
    @Json(name = "githubCommitSha") val githubCommitSha: String? = null,
    @Json(name = "githubCommitAuthorName") val githubCommitAuthorName: String? = null
)

@JsonClass(generateAdapter = true)
data class VercelDomainsResponse(
    @Json(name = "domains") val domains: List<VercelDomain> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VercelDomain(
    @Json(name = "name") val name: String,
    @Json(name = "verified") val verified: Boolean? = true,
    @Json(name = "createdAt") val createdAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class VercelEnvResponse(
    @Json(name = "envs") val envs: List<VercelEnvVar> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VercelEnvVar(
    @Json(name = "id") val id: String? = null,
    @Json(name = "key") val key: String,
    @Json(name = "value") val value: String? = null,
    @Json(name = "type") val type: String? = "plain",
    @Json(name = "target") val target: List<String>? = emptyList(),
    @Json(name = "updatedAt") val updatedAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class RedeployRequest(
    @Json(name = "name") val name: String,
    @Json(name = "deploymentId") val deploymentId: String,
    @Json(name = "target") val target: String? = "production"
)

@JsonClass(generateAdapter = true)
data class CreateProjectRequest(
    @Json(name = "name") val name: String,
    @Json(name = "framework") val framework: String? = null
)

@JsonClass(generateAdapter = true)
data class AddDomainRequest(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class AddEnvVarRequest(
    @Json(name = "key") val key: String,
    @Json(name = "value") val value: String,
    @Json(name = "type") val type: String = "plain",
    @Json(name = "target") val target: List<String> = listOf("production", "preview", "development")
)

data class DeploymentLogItem(
    val id: String,
    val text: String,
    val type: String, // stdout, stderr, info
    val timestamp: Long
)
