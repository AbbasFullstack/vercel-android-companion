package com.example.data.api

import com.example.data.model.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface VercelApiService {

    @GET("v2/user")
    suspend fun getUser(): VercelUserResponse

    @GET("v2/teams")
    suspend fun getTeams(): VercelTeamsResponse

    @GET("v9/projects")
    suspend fun getProjects(
        @Query("teamId") teamId: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null
    ): VercelProjectsResponse

    @GET("v9/projects/{idOrName}")
    suspend fun getProject(
        @Path("idOrName") idOrName: String,
        @Query("teamId") teamId: String? = null
    ): VercelProject

    @POST("v10/projects")
    suspend fun createProject(
        @Body request: CreateProjectRequest,
        @Query("teamId") teamId: String? = null
    ): VercelProject

    @GET("v6/deployments")
    suspend fun getDeployments(
        @Query("projectId") projectId: String? = null,
        @Query("teamId") teamId: String? = null,
        @Query("limit") limit: Int = 30,
        @Query("state") state: String? = null
    ): VercelDeploymentsResponse

    @GET("v13/deployments/{id}")
    suspend fun getDeployment(
        @Path("id") id: String,
        @Query("teamId") teamId: String? = null
    ): VercelDeployment

    @GET("v2/deployments/{id}/events")
    suspend fun getDeploymentEvents(
        @Path("id") id: String,
        @Query("teamId") teamId: String? = null,
        @Query("direction") direction: String = "backward"
    ): ResponseBody

    @POST("v13/deployments")
    suspend fun redeploy(
        @Body request: RedeployRequest,
        @Query("teamId") teamId: String? = null,
        @Query("forceNew") forceNew: Int? = null
    ): VercelDeployment

    @POST("v13/deployments")
    suspend fun createDeployment(
        @Body request: CreateDeploymentRequest,
        @Query("teamId") teamId: String? = null
    ): VercelDeployment

    @PATCH("v12/deployments/{id}/cancel")
    suspend fun cancelDeployment(
        @Path("id") id: String,
        @Query("teamId") teamId: String? = null
    ): VercelDeployment

    @GET("v9/projects/{idOrName}/domains")
    suspend fun getProjectDomains(
        @Path("idOrName") idOrName: String,
        @Query("teamId") teamId: String? = null
    ): VercelDomainsResponse

    @POST("v9/projects/{idOrName}/domains")
    suspend fun addProjectDomain(
        @Path("idOrName") idOrName: String,
        @Body request: AddDomainRequest,
        @Query("teamId") teamId: String? = null
    ): VercelDomain

    @GET("v9/projects/{idOrName}/env")
    suspend fun getProjectEnv(
        @Path("idOrName") idOrName: String,
        @Query("teamId") teamId: String? = null
    ): VercelEnvResponse

    @POST("v10/projects/{idOrName}/env")
    suspend fun addProjectEnv(
        @Path("idOrName") idOrName: String,
        @Body request: AddEnvVarRequest,
        @Query("teamId") teamId: String? = null
    ): ResponseBody
}

class TokenHolder {
    @Volatile
    var token: String? = null
}

class AuthInterceptor(private val tokenHolder: TokenHolder) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        val currentToken = tokenHolder.token
        if (!currentToken.isNullOrBlank()) {
            builder.header("Authorization", "Bearer ${currentToken.trim()}")
        }
        builder.header("User-Agent", "Vercel-Android-Client/1.0")
        return chain.proceed(builder.build())
    }
}

object ApiClient {
    private const val BASE_URL = "https://api.vercel.com/"
    val tokenHolder = TokenHolder()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenHolder))
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: VercelApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(VercelApiService::class.java)

    private val publicOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val gitHubService: GitHubApiService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(publicOkHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(GitHubApiService::class.java)
}
