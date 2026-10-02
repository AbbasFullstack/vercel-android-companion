package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FrameworkBadge(
    framework: String?,
    modifier: Modifier = Modifier
) {
    val cleanName = when (framework?.lowercase()) {
        "nextjs", "next.js" -> "Next.js"
        "vite" -> "Vite"
        "create-react-app", "react" -> "React"
        "astro" -> "Astro"
        "svelte", "sveltekit" -> "Svelte"
        "nuxtjs", "nuxt" -> "Nuxt"
        "remix" -> "Remix"
        "gatsby" -> "Gatsby"
        null, "" -> "Other"
        else -> framework.replaceFirstChar { it.uppercase() }
    }

    val tagColor = when (cleanName) {
        "Next.js" -> VercelWhitePure
        "Vite" -> FrameworkVite
        "React" -> FrameworkReact
        "Astro" -> FrameworkAstro
        "Svelte" -> FrameworkSvelte
        "Nuxt" -> FrameworkVue
        else -> VercelGrayLight
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(VercelSurfaceVariant)
            .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = cleanName,
            color = tagColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
