package com.example.ui.screens.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.TokenEntity
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class BottomNavTab(val title: String, val icon: ImageVector) {
    PROJECTS("Projects", Icons.Default.Folder),
    DEPLOYMENTS("Deployments", Icons.Default.RocketLaunch),
    QUICK_DEPLOY("Deploy Hub", Icons.Default.Bolt),
    ACCOUNT("Account", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    activeToken: TokenEntity?,
    teams: List<VercelTeam>,
    selectedTeam: VercelTeam?,
    projects: List<VercelProject>,
    deployments: List<VercelDeployment>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectPersonal: () -> Unit,
    onSelectTeam: (VercelTeam) -> Unit,
    onRefresh: () -> Unit,
    onProjectClick: (VercelProject) -> Unit,
    onDeploymentClick: (VercelDeployment) -> Unit,
    onViewLogsClick: (VercelDeployment) -> Unit,
    onOpenCreateProject: () -> Unit,
    onOpenRedeploy: (VercelProject) -> Unit,
    onSignOut: () -> Unit,
    gitHubRepos: List<GitHubRepo> = emptyList(),
    isFetchingRepos: Boolean = false,
    connectedGitHubUser: String = "AbbasFullstack",
    onFetchGitHubRepos: (String) -> Unit = {},
    onOneClickImport: (GitHubRepo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(BottomNavTab.PROJECTS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFrameworkFilter by remember { mutableStateOf<String?>(null) }
    var selectedStateFilter by remember { mutableStateOf<String?>(null) }

    // Fast O(1) filtering
    val filteredProjects = remember(projects, searchQuery, selectedFrameworkFilter) {
        if (searchQuery.isBlank() && selectedFrameworkFilter == null) {
            projects
        } else {
            projects.filter { p ->
                val matchesQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    (p.link?.repo?.contains(searchQuery, ignoreCase = true) == true)
                val matchesFramework = selectedFrameworkFilter == null ||
                    p.framework.equals(selectedFrameworkFilter, ignoreCase = true)
                matchesQuery && matchesFramework
            }
        }
    }

    val filteredDeployments = remember(deployments, selectedStateFilter) {
        if (selectedStateFilter == null) {
            deployments
        } else {
            deployments.filter { dpl ->
                dpl.state.equals(selectedStateFilter, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(VercelBlack)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VercelLogo(size = 22.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "/",
                                color = VercelBorderFocus,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ScopeSelector(
                                activeToken = activeToken,
                                teams = teams,
                                selectedTeam = selectedTeam,
                                onSelectPersonal = onSelectPersonal,
                                onSelectTeam = onSelectTeam
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("dashboard_refresh_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = VercelWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = VercelBlack)
                )

                // Thin smooth non-blocking progress bar instead of blocking screen
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = VercelWhitePure,
                        trackColor = VercelBlack
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = VercelSurface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.border(androidx.compose.foundation.BorderStroke(1.dp, VercelBorder))
            ) {
                BottomNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) VercelWhitePure else VercelGray
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) VercelWhitePure else VercelGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VercelWhitePure,
                            selectedTextColor = VercelWhitePure,
                            unselectedIconColor = VercelGray,
                            unselectedTextColor = VercelGray,
                            indicatorColor = VercelSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == BottomNavTab.PROJECTS) {
                FloatingActionButton(
                    onClick = onOpenCreateProject,
                    containerColor = VercelWhitePure,
                    contentColor = VercelBlack,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("create_project_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Project",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        containerColor = VercelBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Error banner
            if (errorMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusErrorBg)
                        .border(1.dp, StatusError.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage,
                        color = StatusError,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = StatusError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Tab Content
            when (currentTab) {
                BottomNavTab.PROJECTS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Situational Summary Card (Instant understanding)
                        item(key = "stats_summary") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VercelSurface)
                                    .border(1.dp, VercelBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SummaryStat(
                                    label = "PROJECTS",
                                    value = "${projects.size}",
                                    color = VercelWhitePure
                                )
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(1.dp)
                                        .background(VercelBorder)
                                )
                                SummaryStat(
                                    label = "RECENT BUILDS",
                                    value = "${deployments.size}",
                                    color = VercelBlueLight
                                )
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(1.dp)
                                        .background(VercelBorder)
                                )
                                SummaryStat(
                                    label = "SCOPE",
                                    value = selectedTeam?.name?.take(10) ?: "Personal",
                                    color = StatusReady
                                )
                            }
                        }

                        // Search Bar
                        item(key = "search_bar") {
                            GeistTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Search projects by name or repo…",
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = VercelGrayLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = VercelGrayLight,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                },
                                testTag = "projects_search_input"
                            )
                        }

                        // Framework Filter Pills
                        item(key = "framework_pills") {
                            val frameworkOptions = listOf(
                                null to "All",
                                "nextjs" to "Next.js",
                                "vite" to "Vite",
                                "create-react-app" to "React",
                                "astro" to "Astro",
                                "remix" to "Remix",
                                "svelte" to "Svelte"
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(frameworkOptions) { (key, label) ->
                                    val isSelected = selectedFrameworkFilter == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) VercelWhitePure else VercelSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) VercelWhitePure else VercelBorder,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedFrameworkFilter = key }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) VercelBlack else VercelGrayLight,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Projects List with key for 0-latency recomposition
                        if (filteredProjects.isEmpty()) {
                            item(key = "empty_projects") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VercelSurface)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(12.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = VercelGray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (searchQuery.isNotEmpty()) "No matching projects" else "No projects in this scope",
                                            color = VercelWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap '+ New Project' below to deploy one.",
                                            color = VercelGrayLight,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = filteredProjects,
                                key = { it.id },
                                contentType = { "project_card" }
                            ) { project ->
                                ProjectCard(
                                    project = project,
                                    onProjectClick = { onProjectClick(project) },
                                    onRedeployClick = { onOpenRedeploy(project) }
                                )
                            }
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }

                BottomNavTab.DEPLOYMENTS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "deployment_filters") {
                            val stateOptions = listOf(
                                null to "All States",
                                "READY" to "Ready",
                                "BUILDING" to "Building",
                                "ERROR" to "Error",
                                "CANCELED" to "Canceled"
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                items(stateOptions) { (key, label) ->
                                    val isSelected = selectedStateFilter == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) VercelWhitePure else VercelSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) VercelWhitePure else VercelBorder,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedStateFilter = key }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) VercelBlack else VercelGrayLight,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredDeployments.isEmpty()) {
                            item(key = "empty_deployments") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VercelSurface)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(12.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            tint = VercelGray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "No deployments found",
                                            color = VercelWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = filteredDeployments,
                                key = { it.uid },
                                contentType = { "deployment_card" }
                            ) { deployment ->
                                DeploymentCard(
                                    deployment = deployment,
                                    onDeploymentClick = { onDeploymentClick(deployment) },
                                    onViewLogsClick = { onViewLogsClick(deployment) }
                                )
                            }
                        }

                        item(key = "bottom_spacer_dpl") {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                BottomNavTab.QUICK_DEPLOY -> {
                    // Action Center / Deploy Hub
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "DEPLOYMENT ACTION HUB",
                                color = VercelGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        // Wizard Launcher Card 1: Create New Project
                        item {
                            GeistCard(
                                onClick = onOpenCreateProject,
                                testTag = "hub_new_project_card"
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(VercelBlue.copy(alpha = 0.15f))
                                            .border(1.dp, VercelBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircleOutline,
                                            contentDescription = null,
                                            tint = VercelBlueLight,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Deploy New Project Wizard",
                                            color = VercelWhitePure,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "37 Framework Presets & Custom Configuration",
                                            color = VercelGrayLight,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = VercelGrayLight
                                    )
                                }
                            }
                        }

                        // Connected GitHub Repositories (1-Click Import)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StatusReady)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CONNECTED GITHUB: @$connectedGitHubUser",
                                        color = VercelWhitePure,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = "1-Click Import ⚡",
                                    color = StatusReady,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isFetchingRepos) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = VercelWhitePure,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else if (gitHubRepos.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VercelSurfaceVariant)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Fetch repositories for @$connectedGitHubUser",
                                        color = VercelGrayLight,
                                        fontSize = 12.sp
                                    )
                                    Button(
                                        onClick = { onFetchGitHubRepos(connectedGitHubUser) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VercelWhitePure,
                                            contentColor = VercelBlack
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Fetch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            items(gitHubRepos, key = { it.id }) { repo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VercelSurfaceVariant)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = repo.name,
                                            color = VercelWhitePure,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!repo.description.isNullOrBlank()) {
                                            Text(
                                                text = repo.description,
                                                color = VercelGrayLight,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (!repo.language.isNullOrBlank()) {
                                                Text(
                                                    text = repo.language,
                                                    color = VercelBlueLight,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            Text(
                                                text = "⭐ ${repo.stars}",
                                                color = VercelGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onOneClickImport(repo) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VercelWhitePure,
                                            contentColor = VercelBlack
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("⚡ 1-Click Import", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Quick Redeploy Picker Card
                        item {
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "1-TAP QUICK REDEPLOY",
                                    color = VercelGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Select any project to immediately trigger a production redeployment:",
                                    color = VercelWhite,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (projects.isEmpty()) {
                                    Text(
                                        text = "No projects available to redeploy.",
                                        color = VercelGrayLight,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    projects.take(4).forEach { proj ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(VercelSurfaceVariant)
                                                .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                                .clickable { onOpenRedeploy(proj) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                FrameworkBadge(framework = proj.framework)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = proj.name,
                                                    color = VercelWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = VercelBlueLight,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Redeploy",
                                                    color = VercelBlueLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                BottomNavTab.ACCOUNT -> {
                    // Account & Scope Management Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "VERCEL ACCOUNT",
                                color = VercelGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        // Profile Card
                        item {
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (!activeToken?.avatar.isNullOrBlank()) {
                                        AsyncImage(
                                            model = activeToken?.avatar,
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, VercelBorder, CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(VercelSurfaceElevated)
                                                .border(1.dp, VercelBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = activeToken?.username?.take(1)?.uppercase() ?: "V",
                                                color = VercelWhite,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeToken?.name ?: activeToken?.username ?: "User",
                                            color = VercelWhitePure,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${activeToken?.username ?: ""}",
                                            color = VercelGrayLight,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (activeToken?.email != null) {
                                            Text(
                                                text = activeToken.email,
                                                color = VercelGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Scope & Teams Switcher
                        item {
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "SELECT ACTIVE SCOPE",
                                    color = VercelGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Personal Account Option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTeam == null) VercelSurfaceElevated else VercelSurfaceVariant)
                                        .border(
                                            1.dp,
                                            if (selectedTeam == null) VercelWhitePure else VercelBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectPersonal() }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (selectedTeam == null) VercelWhitePure else VercelGrayLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Personal Account (${activeToken?.username})",
                                            color = if (selectedTeam == null) VercelWhitePure else VercelWhite,
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedTeam == null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    if (selectedTeam == null) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = StatusReady,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (teams.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    teams.forEach { team ->
                                        val isTeamActive = selectedTeam?.id == team.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isTeamActive) VercelSurfaceElevated else VercelSurfaceVariant)
                                                .border(
                                                    1.dp,
                                                    if (isTeamActive) VercelWhitePure else VercelBorder,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { onSelectTeam(team) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Groups,
                                                    contentDescription = null,
                                                    tint = if (isTeamActive) VercelWhitePure else VercelGrayLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = team.name,
                                                    color = if (isTeamActive) VercelWhitePure else VercelWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isTeamActive) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                            if (isTeamActive) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Active",
                                                    tint = StatusReady,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Developer & App Credits
                        item {
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    VercelLogo(size = 28.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Vercel Android Client",
                                            color = VercelWhitePure,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Version 1.0.0 • Production Build",
                                            color = VercelGrayLight,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = VercelBorder)
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "ENGINEER & CREATOR",
                                    color = VercelGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VercelSurfaceVariant)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.abbas_profile),
                                        contentDescription = "Abbas Hussain",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, VercelBlueLight, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Abbas Hussain",
                                            color = VercelWhitePure,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Full Stack Developer • @AbbasFullstack",
                                            color = StatusReady,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "📞 03088361404",
                                            color = VercelWhite,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "✉️ abbaswebdevelopers@gmail.com",
                                            color = VercelGrayLight,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "🌐 abbas-portfolio-beta.vercel.app",
                                            color = VercelBlueLight,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Logout action
                        item {
                            GeistButton(
                                text = "Disconnect Vercel Account",
                                onClick = onSignOut,
                                style = GeistButtonStyle.DANGER,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null,
                                        tint = StatusError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "settings_disconnect_btn"
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = VercelGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
