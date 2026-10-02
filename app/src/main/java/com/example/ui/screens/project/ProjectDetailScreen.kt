package com.example.ui.screens.project

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class ProjectTab {
    DEPLOYMENTS,
    DOMAINS,
    ENV_VARS,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: VercelProject,
    deployments: List<VercelDeployment>,
    domains: List<VercelDomain>,
    envVars: List<VercelEnvVar>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRedeployClick: () -> Unit,
    onDeploymentClick: (VercelDeployment) -> Unit,
    onViewLogsClick: (VercelDeployment) -> Unit,
    onAddDomain: (domain: String) -> Unit,
    onAddEnvVar: (key: String, value: String, target: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(ProjectTab.DEPLOYMENTS) }
    var showAddDomainDialog by remember { mutableStateOf(false) }
    var showAddEnvDialog by remember { mutableStateOf(false) }

    val prod = project.targets?.get("production")
    val prodUrl = prod?.url

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = project.name,
                        color = VercelWhitePure,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("project_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = VercelWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("project_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = VercelWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VercelBlack
                )
            )
        },
        containerColor = VercelBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Project Overview Card
            item {
                GeistCard(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FrameworkBadge(framework = project.framework)
                            if (project.link?.repo != null) {
                                Text(
                                    text = project.link.repo,
                                    color = VercelGrayLight,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        GeistStatusBadge(state = prod?.readyState ?: "READY")
                    }

                    if (!prodUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(VercelSurfaceVariant)
                                .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = VercelBlueLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prodUrl,
                                color = VercelWhite,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val full = "https://$prodUrl"
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(full)))
                                    }
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("https://$prodUrl"))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = VercelGrayLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GeistButton(
                        text = "Redeploy Production",
                        onClick = onRedeployClick,
                        style = GeistButtonStyle.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = VercelBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "project_redeploy_btn"
                    )
                }
            }

            // Tab Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VercelSurfaceElevated)
                        .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProjectTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val label = when (tab) {
                            ProjectTab.DEPLOYMENTS -> "Deployments"
                            ProjectTab.DOMAINS -> "Domains"
                            ProjectTab.ENV_VARS -> "Env Vars"
                            ProjectTab.SETTINGS -> "Settings"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VercelWhitePure else VercelSurfaceElevated)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) VercelBlack else VercelGrayLight,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tab Contents
            when (selectedTab) {
                ProjectTab.DEPLOYMENTS -> {
                    if (deployments.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No deployments found for this project.",
                                    color = VercelGrayLight,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(deployments) { deployment ->
                            DeploymentCard(
                                deployment = deployment,
                                onDeploymentClick = { onDeploymentClick(deployment) },
                                onViewLogsClick = { onViewLogsClick(deployment) }
                            )
                        }
                    }
                }

                ProjectTab.DOMAINS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Domains (${domains.size})",
                                color = VercelWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            GeistButton(
                                text = "+ Add Domain",
                                onClick = { showAddDomainDialog = true },
                                style = GeistButtonStyle.SECONDARY,
                                modifier = Modifier.height(36.dp),
                                testTag = "add_domain_open_modal"
                            )
                        }
                    }

                    if (domains.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No custom domains configured.",
                                    color = VercelGrayLight,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(domains) { domain ->
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = domain.name,
                                            color = VercelWhitePure,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (domain.verified == true) "Verified & Active" else "Pending Verification",
                                            color = if (domain.verified == true) StatusReady else StatusBuilding,
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://${domain.name}"))
                                            )
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open",
                                            tint = VercelGrayLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ProjectTab.ENV_VARS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Environment Variables (${envVars.size})",
                                color = VercelWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            GeistButton(
                                text = "+ Add Variable",
                                onClick = { showAddEnvDialog = true },
                                style = GeistButtonStyle.SECONDARY,
                                modifier = Modifier.height(36.dp),
                                testTag = "add_env_open_modal"
                            )
                        }
                    }

                    if (envVars.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No environment variables set.",
                                    color = VercelGrayLight,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(envVars) { env ->
                            var isRevealed by remember { mutableStateOf(false) }

                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = env.key,
                                        color = VercelWhitePure,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    IconButton(
                                        onClick = { isRevealed = !isRevealed },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle value",
                                            tint = VercelGrayLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isRevealed) (env.value ?: "••••••••") else "••••••••••••••••",
                                    color = VercelGrayLight,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    (env.target ?: listOf("production")).forEach { target ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(VercelSurfaceElevated)
                                                .border(1.dp, VercelBorder, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = target,
                                                color = VercelGrayLight,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ProjectTab.SETTINGS -> {
                    item {
                        GeistCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "PROJECT SETTINGS",
                                color = VercelGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            SettingsRow(label = "Project ID", value = project.id)
                            SettingsRow(label = "Framework", value = project.framework ?: "Other")
                            SettingsRow(label = "Created", value = formatRelativeTime(project.createdAt))
                            SettingsRow(label = "Updated", value = formatRelativeTime(project.updatedAt))
                            if (project.link?.repo != null) {
                                SettingsRow(label = "Git Repository", value = project.link.repo)
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

    // Add Domain Dialog
    if (showAddDomainDialog) {
        AddDomainDialog(
            onDismiss = { showAddDomainDialog = false },
            onConfirm = { domain ->
                onAddDomain(domain)
                showAddDomainDialog = false
            }
        )
    }

    // Add Env Var Dialog
    if (showAddEnvDialog) {
        AddEnvVarDialog(
            onDismiss = { showAddEnvDialog = false },
            onConfirm = { key, value, targets ->
                onAddEnvVar(key, value, targets)
                showAddEnvDialog = false
            }
        )
    }
}

@Composable
fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = VercelGrayLight, fontSize = 13.sp)
        Text(
            text = value,
            color = VercelWhite,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun AddDomainDialog(
    onDismiss: () -> Unit,
    onConfirm: (domain: String) -> Unit
) {
    var domain by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Add Custom Domain",
                color = VercelWhitePure,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            GeistTextField(
                value = domain,
                onValueChange = { domain = it },
                placeholder = "e.g. app.mycompany.com",
                isMonospace = true,
                testTag = "input_domain_name"
            )
            Spacer(modifier = Modifier.height(16.dp))
            GeistButton(
                text = "Add Domain",
                onClick = { onConfirm(domain.trim()) },
                enabled = domain.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_domain_btn"
            )
        }
    }
}

@Composable
fun AddEnvVarDialog(
    onDismiss: () -> Unit,
    onConfirm: (key: String, value: String, targets: List<String>) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Add Environment Variable",
                color = VercelWhitePure,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "KEY", color = VercelGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            GeistTextField(
                value = key,
                onValueChange = { key = it },
                placeholder = "DATABASE_URL",
                isMonospace = true,
                testTag = "input_env_key"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "VALUE", color = VercelGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            GeistTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "secret_value_here",
                isMonospace = true,
                testTag = "input_env_value"
            )

            Spacer(modifier = Modifier.height(16.dp))

            GeistButton(
                text = "Save Variable",
                onClick = {
                    onConfirm(
                        key.trim(),
                        value.trim(),
                        listOf("production", "preview", "development")
                    )
                },
                enabled = key.isNotBlank() && value.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_env_btn"
            )
        }
    }
}
