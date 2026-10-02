package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VercelDeployment
import com.example.ui.theme.*

@Composable
fun DeploymentCard(
    deployment: VercelDeployment,
    onDeploymentClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = deployment.meta
    val commitMsg = meta?.githubCommitMessage ?: "Manual deployment"
    val branch = meta?.githubCommitRef ?: "main"
    val sha = meta?.githubCommitSha?.take(7)
    val author = meta?.githubCommitAuthorName ?: deployment.creator?.username ?: "User"
    val isProduction = deployment.target?.equals("production", ignoreCase = true) == true

    GeistCard(
        onClick = onDeploymentClick,
        modifier = modifier.testTag("deployment_card_${deployment.uid}")
    ) {
        // Top row: Project name / URL + Target badge + Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deployment.name,
                    color = VercelWhitePure,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isProduction) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VercelBlue.copy(alpha = 0.15f))
                            .border(1.dp, VercelBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Production",
                            color = VercelBlueLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            GeistStatusBadge(state = deployment.state)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Commit Message
        Text(
            text = commitMsg,
            color = VercelWhite,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Git details: branch + commit SHA + author
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ForkRight,
                contentDescription = "Branch",
                tint = VercelGrayLight,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = branch,
                color = VercelGrayLight,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            if (sha != null) {
                Text(
                    text = "•",
                    color = VercelGray,
                    fontSize = 11.sp
                )
                Text(
                    text = sha,
                    color = VercelGrayLight,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom: Author avatar + relative time + Logs CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!deployment.creator?.avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = deployment.creator?.avatar,
                        contentDescription = author,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = author,
                    color = VercelGrayLight,
                    fontSize = 12.sp
                )
                Text(
                    text = "•",
                    color = VercelGray,
                    fontSize = 11.sp
                )
                Text(
                    text = formatRelativeTime(deployment.created),
                    color = VercelGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick view logs button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VercelSurfaceVariant)
                    .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onViewLogsClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("logs_btn_${deployment.uid}")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Logs",
                    tint = VercelWhite,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Logs",
                    color = VercelWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
