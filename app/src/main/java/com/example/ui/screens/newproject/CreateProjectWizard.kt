package com.example.ui.screens.newproject

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.VercelTeam
import com.example.ui.components.*
import com.example.ui.theme.*

enum class ProjectWizardStep(val index: Int, val title: String) {
    NAME(1, "Name"),
    FRAMEWORK(2, "Framework"),
    CONFIRM(3, "Review")
}

@Composable
fun CreateProjectWizard(
    selectedTeam: VercelTeam?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateProject: (name: String, framework: String?) -> Unit
) {
    var step by remember { mutableStateOf(ProjectWizardStep.NAME) }
    var projectName by remember { mutableStateOf("") }
    var selectedFramework by remember { mutableStateOf("nextjs") }

    val frameworks = listOf(
        Triple("nextjs", "Next.js", FrameworkNextJs),
        Triple("vite", "Vite", FrameworkVite),
        Triple("create-react-app", "React", FrameworkReact),
        Triple("astro", "Astro", FrameworkAstro),
        Triple("remix", "Remix", VercelBlueLight),
        Triple("svelte", "Svelte", FrameworkSvelte),
        Triple("nuxtjs", "Nuxt", FrameworkVue),
        Triple("other", "Other / Static", VercelWhite)
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("create_project_wizard")
        ) {
            // Header: Title + Step counter + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "New Project Wizard",
                        color = VercelWhitePure,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step ${step.index} of 3 • ${step.title}",
                        color = VercelGrayLight,
                        fontSize = 11.sp
                    )
                }

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

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProjectWizardStep.values().forEach { s ->
                    val isDone = step.index > s.index
                    val isCurrent = step == s
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isDone -> StatusReady
                                    isCurrent -> VercelWhitePure
                                    else -> VercelBorder
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                },
                label = "step_content"
            ) { currentStep ->
                when (currentStep) {
                    ProjectWizardStep.NAME -> {
                        Column {
                            Text(
                                text = "What would you like to call your project?",
                                color = VercelWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Use letters, numbers, and dashes.",
                                color = VercelGrayLight,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            GeistTextField(
                                value = projectName,
                                onValueChange = {
                                    projectName = it.lowercase().replace(" ", "-").filter { c ->
                                        c.isLetterOrDigit() || c == '-'
                                    }
                                },
                                placeholder = "my-nextjs-app",
                                isMonospace = true,
                                testTag = "wizard_project_name_input"
                            )

                            if (projectName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VercelSurfaceVariant)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Domain: ",
                                        color = VercelGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "$projectName.vercel.app",
                                        color = VercelBlueLight,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    ProjectWizardStep.FRAMEWORK -> {
                        Column {
                            Text(
                                text = "Select Framework Preset",
                                color = VercelWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                items(frameworks) { (id, label, color) ->
                                    val isSelected = selectedFramework == id
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) VercelSurfaceVariant else VercelSurface)
                                            .border(
                                                1.dp,
                                                if (isSelected) VercelWhitePure else VercelBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedFramework = id }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) VercelWhitePure else VercelGrayLight,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = VercelWhitePure,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ProjectWizardStep.CONFIRM -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Ready to Deploy",
                                color = VercelWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Project Name", color = VercelGrayLight, fontSize = 12.sp)
                                    Text(
                                        text = projectName,
                                        color = VercelWhitePure,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Framework", color = VercelGrayLight, fontSize = 12.sp)
                                    Text(
                                        text = frameworks.find { it.first == selectedFramework }?.second ?: selectedFramework,
                                        color = VercelBlueLight,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Scope", color = VercelGrayLight, fontSize = 12.sp)
                                    Text(
                                        text = selectedTeam?.name ?: "Personal Account",
                                        color = VercelWhite,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Buttons (Back & Next / Create)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step != ProjectWizardStep.NAME) {
                    GeistButton(
                        text = "Back",
                        onClick = {
                            step = when (step) {
                                ProjectWizardStep.FRAMEWORK -> ProjectWizardStep.NAME
                                ProjectWizardStep.CONFIRM -> ProjectWizardStep.FRAMEWORK
                                else -> ProjectWizardStep.NAME
                            }
                        },
                        style = GeistButtonStyle.SECONDARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (step != ProjectWizardStep.CONFIRM) {
                    GeistButton(
                        text = "Next",
                        onClick = {
                            step = when (step) {
                                ProjectWizardStep.NAME -> ProjectWizardStep.FRAMEWORK
                                ProjectWizardStep.FRAMEWORK -> ProjectWizardStep.CONFIRM
                                else -> ProjectWizardStep.CONFIRM
                            }
                        },
                        enabled = projectName.isNotBlank(),
                        style = GeistButtonStyle.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = VercelBlack,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "wizard_next_btn"
                    )
                } else {
                    GeistButton(
                        text = "Deploy Project 🚀",
                        onClick = {
                            onCreateProject(
                                projectName.trim(),
                                if (selectedFramework == "other") null else selectedFramework
                            )
                        },
                        isLoading = isLoading,
                        style = GeistButtonStyle.PRIMARY,
                        modifier = Modifier.weight(1f),
                        testTag = "wizard_deploy_confirm_btn"
                    )
                }
            }
        }
    }
}
