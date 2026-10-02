package com.example.ui.screens.deployment

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.model.DeploymentLogItem
import com.example.data.model.VercelDeployment
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeploymentDetailScreen(
    deployment: VercelDeployment,
    logs: List<DeploymentLogItem>,
    isLogsLoading: Boolean,
    onBack: () -> Unit,
    onRedeploy: () -> Unit,
    onCancelDeployment: () -> Unit,
    onRefreshLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isBuilding = deployment.state in listOf("BUILDING", "INITIALIZING", "QUEUED")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = deployment.name,
                            color = VercelWhitePure,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = deployment.uid.take(12),
                            color = VercelGrayLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("deployment_back_btn")
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
                        onClick = onRefreshLogs,
                        modifier = Modifier.testTag("refresh_logs_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Logs",
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
            // Status and Summary Card
            item {
                GeistCard(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            color = VercelGrayLight,
                            fontSize = 13.sp
                        )
                        GeistStatusBadge(state = deployment.state)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = VercelBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Deployment Domain
                    Text(
                        text = "DOMAIN",
                        color = VercelGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(VercelSurfaceVariant)
                            .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = deployment.url,
                            color = VercelBlueLight,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val full = "https://${deployment.url}"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(full)))
                                }
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("https://${deployment.url}"))
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

                    // Git Commit Details if available
                    val meta = deployment.meta
                    if (meta?.githubCommitMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "COMMIT",
                            color = VercelGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = meta.githubCommitMessage,
                            color = VercelWhite,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (meta.githubCommitRef != null) {
                                Text(
                                    text = meta.githubCommitRef,
                                    color = VercelGrayLight,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (meta.githubCommitSha != null) {
                                Text(
                                    text = "(${meta.githubCommitSha.take(7)})",
                                    color = VercelGrayLight,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Redeploy / Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GeistButton(
                            text = "Redeploy",
                            onClick = onRedeploy,
                            style = GeistButtonStyle.PRIMARY,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = VercelBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "detail_redeploy_btn"
                        )

                        if (isBuilding) {
                            GeistButton(
                                text = "Cancel",
                                onClick = onCancelDeployment,
                                style = GeistButtonStyle.DANGER,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = StatusError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "detail_cancel_btn"
                            )
                        }
                    }
                }
            }

            // Build Logs Console
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = VercelWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Build Logs",
                            color = VercelWhitePure,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isLogsLoading) {
                        CircularProgressIndicator(
                            color = VercelWhite,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (logs.isEmpty() && !isLogsLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VercelSurfaceVariant)
                            .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBuilding) "Waiting for build logs…" else "No build log events found.",
                            color = VercelGrayLight,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                itemsIndexed(logs) { index, log ->
                    val isError = log.type == "stderr" || log.text.contains("error", ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isError) StatusErrorBg else VercelBlack)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${index + 1}".padStart(3, ' '),
                            color = VercelGrayDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(32.dp)
                        )
                        Text(
                            text = log.text,
                            color = if (isError) StatusError else VercelWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
