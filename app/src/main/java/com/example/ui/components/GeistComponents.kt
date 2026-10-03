package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class GeistButtonStyle {
    PRIMARY,
    SECONDARY,
    DANGER,
    GHOST
}

@Composable
fun GeistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GeistButtonStyle = GeistButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "geist_button"
) {
    val (bgColor, textColor, borderColor) = when (style) {
        GeistButtonStyle.PRIMARY -> Triple(
            if (enabled) VercelWhitePure else VercelGrayDark,
            VercelBlack,
            Color.Transparent
        )
        GeistButtonStyle.SECONDARY -> Triple(
            VercelSurfaceVariant,
            VercelWhite,
            VercelBorder
        )
        GeistButtonStyle.DANGER -> Triple(
            VercelSurfaceVariant,
            StatusError,
            StatusError.copy(alpha = 0.5f)
        )
        GeistButtonStyle.GHOST -> Triple(
            Color.Transparent,
            VercelGrayLight,
            Color.Transparent
        )
    }

    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        contentColor = textColor,
        border = if (borderColor != Color.Transparent) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null,
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = textColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val DefaultCardShape = RoundedCornerShape(12.dp)

@Composable
fun GeistCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String = "geist_card",
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth(),
        shape = DefaultCardShape,
        color = VercelSurface,
        border = BorderStroke(1.dp, VercelBorder),
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GeistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isMonospace: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "geist_text_field"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(8.dp))
            .background(VercelSurfaceVariant)
            .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = VercelGray,
                        fontSize = 14.sp,
                        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = singleLine,
                    textStyle = TextStyle(
                        color = VercelWhite,
                        fontSize = 14.sp,
                        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
                    ),
                    cursorBrush = SolidColor(VercelWhite),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}
