package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CreatorWelcomeDialog
import com.example.ui.components.RedeployDialog
import com.example.ui.screens.auth.TokenInputScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.deployment.DeploymentDetailScreen
import com.example.ui.screens.newproject.CreateProjectWizard
import com.example.ui.screens.project.ProjectDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VercelBlack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VercelBlack
                ) {
                    VercelApp()
                }
            }
        }
    }
}

@Composable
fun VercelApp(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeToken by viewModel.activeToken.collectAsStateWithLifecycle()
    val allTokens by viewModel.allTokens.collectAsStateWithLifecycle()
    val verifiedUser by viewModel.verifiedUser.collectAsStateWithLifecycle()
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val selectedTeam by viewModel.selectedTeam.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val deployments by viewModel.deployments.collectAsStateWithLifecycle()
    val projectDeployments by viewModel.projectDeployments.collectAsStateWithLifecycle()
    val projectDomains by viewModel.projectDomains.collectAsStateWithLifecycle()
    val projectEnvVars by viewModel.projectEnvVars.collectAsStateWithLifecycle()
    val deploymentLogs by viewModel.deploymentLogs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLogsLoading by viewModel.isLogsLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val projectToRedeploy by viewModel.projectToRedeploy.collectAsStateWithLifecycle()
    val isCreateProjectOpen by viewModel.isCreateProjectOpen.collectAsStateWithLifecycle()
    val gitHubRepos by viewModel.gitHubRepos.collectAsStateWithLifecycle()
    val isFetchingRepos by viewModel.isFetchingRepos.collectAsStateWithLifecycle()
    val gitHubError by viewModel.gitHubError.collectAsStateWithLifecycle()
    val connectedGitHubUser by viewModel.connectedGitHubUser.collectAsStateWithLifecycle()

    var showCreatorWelcome by rememberSaveable { mutableStateOf(true) }

    if (showCreatorWelcome) {
        CreatorWelcomeDialog(
            onDismiss = { showCreatorWelcome = false }
        )
    }

    when (val screen = currentScreen) {
        is Screen.Auth -> {
            TokenInputScreen(
                isLoading = isLoading,
                errorMessage = errorMessage,
                verifiedUser = verifiedUser,
                savedTokens = allTokens,
                onSubmitToken = { token -> viewModel.connectToken(token) },
                onSelectSavedToken = { token -> viewModel.selectSavedToken(token) },
                onProceedToDashboard = { viewModel.proceedToDashboard() }
            )
        }

        is Screen.Dashboard -> {
            DashboardScreen(
                activeToken = activeToken,
                teams = teams,
                selectedTeam = selectedTeam,
                projects = projects,
                deployments = deployments,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onSelectPersonal = { viewModel.selectPersonal() },
                onSelectTeam = { team -> viewModel.selectTeam(team) },
                onRefresh = { viewModel.refreshDashboard() },
                onProjectClick = { project -> viewModel.openProject(project) },
                onDeploymentClick = { deployment -> viewModel.openDeployment(deployment) },
                onViewLogsClick = { deployment -> viewModel.openDeployment(deployment) },
                onOpenCreateProject = { viewModel.openCreateProject() },
                onOpenRedeploy = { project -> viewModel.openRedeployDialog(project) },
                onSignOut = { viewModel.signOut() },
                gitHubRepos = gitHubRepos,
                isFetchingRepos = isFetchingRepos,
                connectedGitHubUser = connectedGitHubUser,
                onFetchGitHubRepos = { u -> viewModel.fetchGitHubRepos(u) },
                onOneClickImport = { repo -> viewModel.oneClickImport(repo) }
            )
        }

        is Screen.ProjectDetail -> {
            ProjectDetailScreen(
                project = screen.project,
                deployments = projectDeployments,
                domains = projectDomains,
                envVars = projectEnvVars,
                isLoading = isLoading,
                onBack = { viewModel.navigateBack() },
                onRefresh = { viewModel.loadProjectDetails(screen.project) },
                onRedeployClick = { viewModel.openRedeployDialog(screen.project) },
                onDeploymentClick = { deployment -> viewModel.openDeployment(deployment) },
                onViewLogsClick = { deployment -> viewModel.openDeployment(deployment) },
                onAddDomain = { domain -> viewModel.addDomain(screen.project, domain) },
                onAddEnvVar = { key, value, targets -> viewModel.addEnvVar(screen.project, key, value, targets) }
            )
        }

        is Screen.DeploymentDetail -> {
            DeploymentDetailScreen(
                deployment = screen.deployment,
                logs = deploymentLogs,
                isLogsLoading = isLogsLoading,
                onBack = { viewModel.navigateBack() },
                onRedeploy = {
                    val proj = projects.find { it.name == screen.deployment.name }
                    if (proj != null) {
                        viewModel.openRedeployDialog(proj)
                    } else {
                        viewModel.triggerRedeploy(forceNew = false)
                    }
                },
                onCancelDeployment = { viewModel.cancelDeployment(screen.deployment) },
                onRefreshLogs = { viewModel.loadDeploymentLogs(screen.deployment.uid) }
            )
        }
    }

    // Modal Dialogs
    if (projectToRedeploy != null) {
        RedeployDialog(
            projectName = projectToRedeploy!!.name,
            onDismiss = { viewModel.closeRedeployDialog() },
            onConfirmRedeploy = { forceNew ->
                viewModel.triggerRedeploy(forceNew = forceNew)
            }
        )
    }

    if (isCreateProjectOpen) {
        CreateProjectWizard(
            selectedTeam = selectedTeam,
            gitHubRepos = gitHubRepos,
            isFetchingRepos = isFetchingRepos,
            gitHubError = gitHubError,
            isLoading = isLoading,
            onFetchRepos = { username -> viewModel.fetchGitHubRepos(username) },
            onDismiss = { viewModel.closeCreateProject() },
            onCreateProject = { name, framework, gitRepo, rootDir, branch ->
                viewModel.createProject(name, framework, gitRepo, rootDir, branch)
            }
        )
    }
}
