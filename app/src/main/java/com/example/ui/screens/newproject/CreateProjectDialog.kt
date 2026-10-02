package com.example.ui.screens.newproject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CreateProjectDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateProject: (name: String, framework: String?) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var selectedFramework by remember { mutableStateOf("nextjs") }

    val frameworks = listOf(
        "nextjs" to "Next.js",
        "vite" to "Vite",
        "create-react-app" to "React",
        "astro" to "Astro",
        "remix" to "Remix",
        "svelte" to "Svelte",
        "nuxtjs" to "Nuxt",
        "other" to "Other"
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("create_project_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Project",
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PROJECT NAME",
                color = VercelGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            GeistTextField(
                value = projectName,
                onValueChange = {
                    projectName = it.lowercase().replace(" ", "-")
                },
                placeholder = "my-awesome-app",
                isMonospace = true,
                testTag = "input_project_name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FRAMEWORK PRESET",
                color = VercelGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(frameworks) { (id, label) ->
                    val isSelected = selectedFramework == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) VercelWhitePure else VercelSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) VercelWhitePure else VercelBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFramework = id }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("framework_option_$id")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) VercelBlack else VercelWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GeistButton(
                text = "Create Project",
                onClick = {
                    onCreateProject(
                        projectName.trim(),
                        if (selectedFramework == "other") null else selectedFramework
                    )
                },
                enabled = projectName.isNotBlank(),
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                testTag = "confirm_create_project_button"
            )
        }
    }
}
