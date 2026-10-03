package com.example.ui.screens.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
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
    var tokenText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isGuideExpanded by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Detect if clipboard has plausible token on launch
    val clipboardText = remember { clipboardManager.getText()?.text }
    val hasPlausibleTokenInClipboard = remember(clipboardText) {
        !clipboardText.isNullOrBlank() && clipboardText.trim().length >= 20
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VercelBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Subtle top gradient glow (Geist dark aura)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF222222),
                            Color(0xFF0F0F0F),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Glowing Vercel Triangle Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(VercelSurfaceElevated)
                    .border(1.dp, VercelBorderFocus, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                VercelLogo(size = 38.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edition Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VercelSurfaceVariant)
                    .border(1.dp, VercelBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(StatusReady)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VERCEL ANDROID CLIENT",
                    color = VercelWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Headline
            Text(
                text = "Deploy instantly.\nMonitor anywhere.",
                color = VercelWhitePure,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "High-speed native dashboard for your Vercel projects, live build logs, and 1-tap redeployments.",
                color = VercelGrayLight,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // If user is already verified: Show Welcome Card
            if (verifiedUser != null) {
                GeistCard(
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "verified_welcome_card"
                ) {
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
                            text = "Connected Successfully!",
                            color = StatusReady,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(VercelSurfaceVariant)
                            .border(1.dp, VercelBorder, RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!verifiedUser.avatar.isNullOrBlank()) {
                            AsyncImage(
                                model = verifiedUser.avatar,
                                contentDescription = verifiedUser.username,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, VercelBorder, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(VercelSurfaceElevated)
                                    .border(1.dp, VercelBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = verifiedUser.username.take(1).uppercase(),
                                    color = VercelWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = verifiedUser.name ?: verifiedUser.username,
                                color = VercelWhitePure,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${verifiedUser.username}",
                                color = VercelGrayLight,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (verifiedUser.email != null) {
                                Text(
                                    text = verifiedUser.email,
                                    color = VercelGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    GeistButton(
                        text = "Open Vercel Dashboard 🚀",
                        onClick = onProceedToDashboard,
                        style = GeistButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "launch_dashboard_verified_btn"
                    )
                }
            } else {
                // Primary Authentication Card
                GeistCard(modifier = Modifier.fillMaxWidth()) {
                    // Clipboard quick detect pill
                    if (hasPlausibleTokenInClipboard && tokenText.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VercelBlue.copy(alpha = 0.12f))
                                .border(1.dp, VercelBlue.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!clipboardText.isNullOrBlank()) {
                                        tokenText = clipboardText.trim()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = VercelBlueLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Token found in clipboard — Tap to Paste",
                                color = VercelBlueLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Paste",
                                color = VercelWhitePure,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Field Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERSONAL ACCESS TOKEN",
                            color = VercelGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "Get Token ↗",
                            color = VercelBlueLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://vercel.com/account/tokens")
                                )
                                context.startActivity(intent)
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
                        testTag = "auth_token_input"
                    )

                    // Error Message
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connect Button
                    GeistButton(
                        text = if (isLoading) "Verifying with Vercel API…" else "Connect Account →",
                        onClick = { onSubmitToken(tokenText) },
                        enabled = tokenText.length >= 10,
                        isLoading = isLoading,
                        style = GeistButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "auth_connect_btn"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Collapsible Guide
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isGuideExpanded = !isGuideExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isGuideExpanded) Icons.Default.ExpandLess else Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = VercelGrayLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGuideExpanded) "Hide token instructions" else "How to create a token in 30 seconds",
                            color = VercelGrayLight,
                            fontSize = 12.sp
                        )
                    }

                    AnimatedVisibility(visible = isGuideExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VercelSurfaceVariant)
                                .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            GuideBullet(number = "1", text = "Open vercel.com in your browser & sign in.")
                            GuideBullet(number = "2", text = "Navigate to Account Settings > Tokens.")
                            GuideBullet(number = "3", text = "Click 'Create Token' (Scope: Full Account).")
                            GuideBullet(number = "4", text = "Copy the token and return here to connect.")

                            Spacer(modifier = Modifier.height(10.dp))

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
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Saved Accounts if any
            if (savedTokens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = VercelBorder)
                    Text(
                        text = "  SAVED ACCOUNTS  ",
                        color = VercelGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(VercelSurfaceElevated)
                                    .border(1.dp, VercelBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = token.username.take(1).uppercase(),
                                    color = VercelWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = token.name ?: token.username,
                                    color = VercelWhitePure,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
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

            Spacer(modifier = Modifier.height(28.dp))

            // Feature Highlights (Clean 2x2 grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeaturePill(
                    icon = Icons.Default.Bolt,
                    title = "0ms Latency",
                    subtitle = "Room cache-first",
                    modifier = Modifier.weight(1f)
                )
                FeaturePill(
                    icon = Icons.Default.Refresh,
                    title = "1-Tap Deploy",
                    subtitle = "Cache & fresh builds",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeaturePill(
                    icon = Icons.Default.Terminal,
                    title = "Live Terminal",
                    subtitle = "Real-time build logs",
                    modifier = Modifier.weight(1f)
                )
                FeaturePill(
                    icon = Icons.Default.Code,
                    title = "Git Import",
                    subtitle = "Deploy GitHub repos",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Author attribution footer
            Text(
                text = "Engineered by Abbas Hussain • @AbbasFullstack",
                color = VercelGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GuideBullet(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(VercelBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = VercelWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = VercelWhite,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(VercelSurface)
            .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VercelBlueLight,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = VercelWhitePure,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = VercelGray,
                fontSize = 10.sp
            )
        }
    }
}
