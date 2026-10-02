package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.VercelProject
import com.example.ui.theme.*

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0) return "Never"
    val now = System.currentTimeMillis()
    val diff = (now - timestamp) / 1000
    return when {
        diff < 60 -> "just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        diff < 604800 -> "${diff / 86400}d ago"
        else -> "${diff / 604800}w ago"
    }
}

@Composable
fun ProjectCard(
    project: VercelProject,
    onProjectClick: () -> Unit,
    onRedeployClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val prod = project.targets?.get("production")
    val prodUrl = prod?.url
    val prodState = prod?.readyState ?: "READY"

    GeistCard(
        onClick = onProjectClick,
        modifier = modifier.testTag("project_card_${project.name}")
    ) {
        // Header: Name + Framework + Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    color = VercelWhitePure,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (project.link?.repo != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Source,
                            contentDescription = "Repo",
                            tint = VercelGrayLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = project.link.repo,
                            color = VercelGrayLight,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            GeistStatusBadge(state = prodState)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Production Domain Pill
        if (!prodUrl.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VercelSurfaceVariant)
                    .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        val fullUrl = if (prodUrl.startsWith("http")) prodUrl else "https://$prodUrl"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Domain",
                    tint = VercelBlueLight,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = prodUrl,
                    color = VercelWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        val fullUrl = if (prodUrl.startsWith("http")) prodUrl else "https://$prodUrl"
                        clipboardManager.setText(AnnotatedString(fullUrl))
                    },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = VercelGrayLight,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Footer: Framework tag + Relative Time + Actions
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
                Text(
                    text = formatRelativeTime(if (prod?.createdAt != null && prod.createdAt > 0) prod.createdAt else project.updatedAt),
                    color = VercelGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Redeploy button
                IconButton(
                    onClick = onRedeployClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VercelSurfaceVariant)
                        .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                        .testTag("redeploy_btn_${project.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Redeploy",
                        tint = VercelWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Details button
                IconButton(
                    onClick = onProjectClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VercelSurfaceVariant)
                        .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                        .testTag("details_btn_${project.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = VercelWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
