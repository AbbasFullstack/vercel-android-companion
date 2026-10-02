package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun RedeployDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirmRedeploy: (forceNew: Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("redeploy_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Redeploy Project",
                    color = VercelWhitePure,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = VercelGrayLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Trigger a new deployment for $projectName with the latest production configuration.",
                color = VercelGrayLight,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: Standard Redeploy (Use Cache)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VercelSurfaceVariant)
                    .border(1.dp, VercelBorder, RoundedCornerShape(10.dp))
                    .clickable { onConfirmRedeploy(false) }
                    .padding(14.dp)
                    .testTag("redeploy_with_cache_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cached,
                    contentDescription = null,
                    tint = VercelBlueLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Redeploy with Cache",
                        color = VercelWhitePure,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fast build reusing existing build cache artifacts.",
                        color = VercelGrayLight,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Clean Redeploy without Cache
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VercelSurfaceVariant)
                    .border(1.dp, VercelBorder, RoundedCornerShape(10.dp))
                    .clickable { onConfirmRedeploy(true) }
                    .padding(14.dp)
                    .testTag("redeploy_without_cache_button")
            ) {
                Icon(
                    imageVector = Icons.Default.LayersClear,
                    contentDescription = null,
                    tint = StatusBuilding,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Redeploy without Cache",
                        color = VercelWhitePure,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Clean build from scratch without any cached artifacts.",
                        color = VercelGrayLight,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
