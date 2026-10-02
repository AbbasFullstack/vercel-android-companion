package com.example.ui.screens.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.TokenEntity
import com.example.data.model.VercelUser
import com.example.ui.components.*
import com.example.ui.theme.*

enum class AuthWizardStep(val index: Int, val title: String) {
    STEP_GUIDE(1, "Get Token"),
    STEP_INPUT(2, "Connect Token"),
    STEP_VERIFIED(3, "Ready")
}

@Composable
fun TokenInputScreen(
    isLoading: Boolean,
    errorMessage: String?,
    verifiedUser: VercelUser?,
    savedTokens: List<TokenEntity>,
    onSubmitToken: (String) -> Unit,
    onSelectSavedToken: (TokenEntity) -> Unit,
    onProceedToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember {
        mutableStateOf(if (verifiedUser != null) AuthWizardStep.STEP_VERIFIED else AuthWizardStep.STEP_INPUT)
    }
    var tokenText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Automatically advance to Step 3 when user is verified
    LaunchedEffect(verifiedUser) {
        if (verifiedUser != null) {
            currentStep = AuthWizardStep.STEP_VERIFIED
        }
    }

    // Check if clipboard contains a plausible token on launch
    val clipboardText = remember { clipboardManager.getText()?.text }
    val isPlausibleTokenInClipboard = remember(clipboardText) {
        !clipboardText.isNullOrBlank() && (clipboardText.length >= 20)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VercelBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Vercel Logo + Title
            Spacer(modifier = Modifier.height(16.dp))
            VercelLogo(size = 40.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Vercel Mobile Setup",
                color = VercelWhitePure,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage your projects, deployments & logs at lightning speed",
                color = VercelGrayLight,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Progress Indicator Wizard Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VercelSurfaceVariant)
                    .border(1.dp, VercelBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthWizardStep.values().forEach { step ->
                    val isCurrent = currentStep == step
                    val isDone = currentStep.index > step.index

                    val pillBg = when {
                        isCurrent -> VercelWhitePure
                        isDone -> StatusReadyBg
                        else -> Color.Transparent
                    }
                    val pillText = when {
                        isCurrent -> VercelBlack
                        isDone -> StatusReady
                        else -> VercelGray
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(pillBg)
                            .clickable {
                                if (step == AuthWizardStep.STEP_VERIFIED && verifiedUser == null) {
                                    // Can't jump to verified unless verified
                                } else {
                                    currentStep = step
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) VercelBlack else pillText),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VercelBlack,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Text(
                                    text = "${step.index}",
                                    color = if (isCurrent) VercelWhitePure else VercelBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = step.title,
                            color = pillText,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wizard Step Content with Smooth Animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "wizard_step_transition"
            ) { step ->
                when (step) {
                    AuthWizardStep.STEP_GUIDE -> {
                        // Step 1: Guide on how to generate a token
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "3 SIMPLE STEPS TO GET YOUR TOKEN",
                                    color = VercelGrayLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                WizardInstructionItem(
                                    number = "1",
                                    title = "Open Vercel Account Settings",
                                    desc = "Log into vercel.com in your browser and visit Account Settings > Tokens."
                                )
                                WizardInstructionItem(
                                    number = "2",
                                    title = "Click 'Create Token'",
                                    desc = "Give it a name (e.g. 'Vercel Android') and set desired expiration."
                                )
                                WizardInstructionItem(
                                    number = "3",
                                    title = "Copy & Return",
                                    desc = "Copy the token secret and paste it into this app."
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                GeistButton(
                                    text = "Open Vercel Tokens Page ↗",
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://vercel.com/account/tokens")
                                        )
                                        context.startActivity(intent)
                                    },
                                    style = GeistButtonStyle.SECONDARY,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "open_vercel_tokens_guide_btn"
                                )
                            }

                            GeistButton(
                                text = "I Have My Token → Continue",
                                onClick = { currentStep = AuthWizardStep.STEP_INPUT },
                                style = GeistButtonStyle.PRIMARY,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "goto_step_2_btn"
                            )
                        }
                    }

                    AuthWizardStep.STEP_INPUT -> {
                        // Step 2: Input & Connect Token
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Quick Clipboard detection card
                            if (isPlausibleTokenInClipboard && tokenText.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VercelBlue.copy(alpha = 0.12f))
                                        .border(1.dp, VercelBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (!clipboardText.isNullOrBlank()) {
                                                tokenText = clipboardText.trim()
                                            }
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        tint = VercelBlueLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Token detected in clipboard!",
                                            color = VercelWhitePure,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Tap here to paste automatically.",
                                            color = VercelGrayLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "Paste",
                                        color = VercelBlueLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VERCEL ACCESS TOKEN",
                                        color = VercelGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Need help?",
                                        color = VercelBlueLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable {
                                            currentStep = AuthWizardStep.STEP_GUIDE
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                GeistTextField(
                                    value = tokenText,
                                    onValueChange = { tokenText = it.trim() },
                                    placeholder = "Paste your personal token here…",
                                    isMonospace = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (tokenText.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { tokenText = "" },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = VercelGrayLight,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            IconButton(
                                                onClick = { isPasswordVisible = !isPasswordVisible },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Toggle visibility",
                                                    tint = VercelGrayLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    testTag = "wizard_token_input"
                                )

                                if (errorMessage != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StatusErrorBg)
                                            .border(1.dp, StatusError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
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
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                GeistButton(
                                    text = if (isLoading) "Verifying with Vercel…" else "Connect & Verify Account",
                                    onClick = { onSubmitToken(tokenText) },
                                    enabled = tokenText.length >= 10,
                                    isLoading = isLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "wizard_submit_token_btn"
                                )
                            }
                        }
                    }

                    AuthWizardStep.STEP_VERIFIED -> {
                        // Step 3: Verified Account Confirmation Card
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GeistCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(StatusReady),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = VercelBlack,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "Account Verified Successfully!",
                                        color = StatusReady,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // User Profile Card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VercelSurfaceVariant)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(10.dp))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!verifiedUser?.avatar.isNullOrBlank()) {
                                        AsyncImage(
                                            model = verifiedUser?.avatar,
                                            contentDescription = verifiedUser?.username,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, VercelBorder, CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(VercelSurfaceElevated)
                                                .border(1.dp, VercelBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = verifiedUser?.username?.take(1)?.uppercase() ?: "V",
                                                color = VercelWhite,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = verifiedUser?.name ?: verifiedUser?.username ?: "Vercel User",
                                            color = VercelWhitePure,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${verifiedUser?.username ?: ""}",
                                            color = VercelGrayLight,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (verifiedUser?.email != null) {
                                            Text(
                                                text = verifiedUser.email,
                                                color = VercelGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                GeistButton(
                                    text = "Launch Vercel Dashboard 🚀",
                                    onClick = onProceedToDashboard,
                                    style = GeistButtonStyle.PRIMARY,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "launch_dashboard_btn"
                                )
                            }
                        }
                    }
                }
            }

            // Saved accounts list if available
            if (savedTokens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = VercelBorder)
                    Text(
                        text = "  SAVED ACCOUNTS  ",
                        color = VercelGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = VercelBorder)
                }

                Spacer(modifier = Modifier.height(12.dp))

                savedTokens.forEach { token ->
                    GeistCard(
                        onClick = { onSelectSavedToken(token) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        testTag = "switch_saved_${token.username}"
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(VercelSurfaceElevated)
                                    .border(1.dp, VercelBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = token.username.take(1).uppercase(),
                                    color = VercelWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = token.name ?: token.username,
                                    color = VercelWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "@${token.username}",
                                    color = VercelGrayLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Use",
                                tint = VercelGrayLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WizardInstructionItem(
    number: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = VercelWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = VercelWhitePure,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = VercelGrayLight,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
