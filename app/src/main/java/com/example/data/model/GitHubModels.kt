package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "private") val isPrivate: Boolean = false,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "stargazers_count") val stars: Int = 0,
    @Json(name = "default_branch") val defaultBranch: String = "main",
    @Json(name = "updated_at") val updatedAt: String? = null
)
