package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CreatorWelcomeDialog(
    onDismiss: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(3) }
    val context = LocalContext.current
    val density = LocalDensity.current.density

    // Smooth progress animation for 3 seconds
    val animatedProgress = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
    }

    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
        onDismiss()
    }

    // 3D Infinite Animations
    val infiniteTransition = rememberInfiniteTransition(label = "3DInfinite")

    // Continuous 360 holographic aura ring rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotate"
    )

    // Gentle 3D floating hover tilt
    val floatTiltY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatTiltY"
    )

    val floatTranslationY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatTransY"
    )

    // Pulse scale for photo badge
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Entrance 3D scale-in
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardEntrance"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // 3D Perspective Card Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        rotationX = 2f
                        rotationY = floatTiltY
                        translationY = floatTranslationY * density
                        cameraDistance = 18f * density
                        shadowElevation = 24.dp.toPx()
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF161616),
                                Color(0xFF0C0C0C),
                                Color(0xFF050505)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF555555),
                                Color(0xFF222222),
                                VercelBlue.copy(alpha = 0.4f),
                                Color(0xFF1A1A1A)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top 3D Pill Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VercelLogo(size = 14.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VERCEL ANDROID CLIENT",
                        color = VercelWhitePure,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3D Animated Holographic Avatar with Rotating Cyber Ring
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating rainbow neon gradient ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(ringRotation)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        VercelBlueLight,
                                        Color(0xFF00DF8F),
                                        Color(0xFF7928CA),
                                        Color(0xFFFF0080),
                                        VercelWhitePure,
                                        VercelBlueLight
                                    )
                                )
                            )
                    )

                    // Inner gap for 3D depth
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0B0B0B))
                    )

                    // Profile Image
                    Image(
                        painter = painterResource(id = R.drawable.abbas_profile),
                        contentDescription = "Abbas Hussain",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(98.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )

                    // 3D Live Status Dot
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(StatusReady)
                            .border(2.dp, Color.Black, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = "Made by Abbas Hussain",
                    color = VercelWhitePure,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Full Stack Developer & Engineer",
                    color = StatusReady,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFF222222))
                Spacer(modifier = Modifier.height(16.dp))

                // 3D Interactive Contact Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InteractiveContactCard3D(
                        icon = Icons.Default.Phone,
                        iconTint = StatusReady,
                        label = "Contact",
                        value = "03088361404",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:03088361404"))
                            context.startActivity(intent)
                        }
                    )

                    InteractiveContactCard3D(
                        icon = Icons.Default.Email,
                        iconTint = VercelBlueLight,
                        label = "Email",
                        value = "abbaswebdevelopers@gmail.com",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:abbaswebdevelopers@gmail.com"))
                            context.startActivity(intent)
                        }
                    )

                    InteractiveContactCard3D(
                        icon = Icons.Default.Code,
                        iconTint = Color(0xFFC084FC),
                        label = "GitHub",
                        value = "@AbbasFullstack",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AbbasFullstack"))
                            context.startActivity(intent)
                        }
                    )

                    InteractiveContactCard3D(
                        icon = Icons.Default.Language,
                        iconTint = Color(0xFF38BDF8),
                        label = "Portfolio",
                        value = "abbas-portfolio-beta.vercel.app",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://abbas-portfolio-beta.vercel.app"))
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3D Animated Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF222222))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.value)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            VercelBlueLight,
                                            StatusReady,
                                            VercelWhitePure
                                        )
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (secondsLeft > 0) "Auto closing in ${secondsLeft}s…" else "Entering app…",
                            color = VercelGrayLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // 3D Continue Button
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VercelWhitePure,
                                contentColor = VercelBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text = "Enter App 🚀",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveContactCard3D(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "contactScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141414))
            .border(
                1.dp,
                if (isPressed) iconTint.copy(alpha = 0.6f) else Color(0xFF262626),
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f))
                .border(1.dp, iconTint.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "$label: ",
            color = VercelGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = VercelWhitePure,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open",
            tint = VercelGrayLight,
            modifier = Modifier.size(14.dp)
        )
    }
}
