package com.example.ui.screens.newproject

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GitHubRepo
import com.example.data.model.VercelTeam
import com.example.ui.components.*
import com.example.ui.theme.*

enum class ProjectWizardStep {
    SELECT_REPO,
    CONFIGURE_PROJECT
}

enum class RepoSourceType(val title: String) {
    GITHUB_REPOS("GitHub Repos"),
    CUSTOM_URL("Git URL"),
    TEMPLATES("Templates")
}

data class VercelFrameworkDef(
    val id: String?,
    val name: String,
    val category: String,
    val color: Color
)

data class StarterTemplate(
    val id: String,
    val title: String,
    val description: String,
    val repo: String,
    val framework: String,
    val badgeColor: Color
)

// Complete official list of 37 Vercel framework presets
val ALL_VERCEL_FRAMEWORKS = listOf(
    VercelFrameworkDef("nextjs", "Next.js", "React", FrameworkNextJs),
    VercelFrameworkDef("vite", "Vite", "Bundler", FrameworkVite),
    VercelFrameworkDef("create-react-app", "Create React App", "React", FrameworkReact),
    VercelFrameworkDef("remix", "Remix", "React", VercelBlueLight),
    VercelFrameworkDef("astro", "Astro", "Multi-framework", FrameworkAstro),
    VercelFrameworkDef("sveltekit", "SvelteKit", "Svelte", FrameworkSvelte),
    VercelFrameworkDef("svelte", "Svelte", "Svelte", FrameworkSvelte),
    VercelFrameworkDef("nuxtjs", "Nuxt.js", "Vue", FrameworkVue),
    VercelFrameworkDef("vue", "Vue.js", "Vue", FrameworkVue),
    VercelFrameworkDef("gatsby", "Gatsby", "React", Color(0xFF663399)),
    VercelFrameworkDef("angular", "Angular", "TypeScript", Color(0xFFDD0031)),
    VercelFrameworkDef("solidstart", "SolidStart", "Solid", Color(0xFF4488EE)),
    VercelFrameworkDef("qwik", "Qwik", "JSX", Color(0xFF18B6F6)),
    VercelFrameworkDef("eleventy", "Eleventy (11ty)", "Static", Color(0xFF222222)),
    VercelFrameworkDef("hexo", "Hexo", "Static", Color(0xFF0E83CD)),
    VercelFrameworkDef("hugo", "Hugo", "Go / Static", Color(0xFFFF4088)),
    VercelFrameworkDef("jekyll", "Jekyll", "Ruby", Color(0xFFCC0000)),
    VercelFrameworkDef("docusaurus-2", "Docusaurus 2", "Documentation", Color(0xFF25C2A0)),
    VercelFrameworkDef("docusaurus", "Docusaurus 1", "Documentation", Color(0xFF25C2A0)),
    VercelFrameworkDef("preact", "Preact", "JavaScript", Color(0xFF673AB8)),
    VercelFrameworkDef("redwoodjs", "RedwoodJS", "Fullstack", Color(0xFFBF4722)),
    VercelFrameworkDef("sanity", "Sanity Studio", "CMS", Color(0xFFF03E2F)),
    VercelFrameworkDef("storybook", "Storybook", "UI Tooling", Color(0xFFFF4785)),
    VercelFrameworkDef("gridsome", "Gridsome", "Vue", Color(0xFF00A672)),
    VercelFrameworkDef("umijs", "UmiJS", "React", Color(0xFF1890FF)),
    VercelFrameworkDef("blitzjs", "Blitz.js", "Fullstack", Color(0xFF6700EB)),
    VercelFrameworkDef("brunch", "Brunch", "Bundler", Color(0xFFF16529)),
    VercelFrameworkDef("ember", "Ember.js", "JavaScript", Color(0xFFE04E39)),
    VercelFrameworkDef("middleman", "Middleman", "Ruby", Color(0xFFF1C40F)),
    VercelFrameworkDef("zola", "Zola", "Rust", Color(0xFF0F9D58)),
    VercelFrameworkDef("hydrogen", "Hydrogen (Shopify)", "React", Color(0xFF95BF47)),
    VercelFrameworkDef("sapper", "Sapper", "Svelte", FrameworkSvelte),
    VercelFrameworkDef("stencil", "Stencil", "Web Components", Color(0xFF4C48FF)),
    VercelFrameworkDef("ionic-react", "Ionic React", "Mobile / Web", Color(0xFF3880FF)),
    VercelFrameworkDef("ionic-angular", "Ionic Angular", "Mobile / Web", Color(0xFF3880FF)),
    VercelFrameworkDef("fasthtml", "FastHTML", "Python", Color(0xFF00C7B7)),
    VercelFrameworkDef(null, "Other / Static HTML", "Static", VercelWhite)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectWizard(
    selectedTeam: VercelTeam?,
    gitHubRepos: List<GitHubRepo>,
    isFetchingRepos: Boolean,
    gitHubError: String?,
    isLoading: Boolean,
    onFetchRepos: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreateProject: (name: String, framework: String?, gitRepo: String?, rootDir: String?, branch: String) -> Unit
) {
    var step by remember { mutableStateOf(ProjectWizardStep.SELECT_REPO) }
    var sourceType by remember { mutableStateOf(RepoSourceType.GITHUB_REPOS) }

    // Account & Repo states
    var gitHubUsernameInput by remember { mutableStateOf("AbbasFullstack") }
    var repoSearchQuery by remember { mutableStateOf("") }
    var selectedRepoFullName by remember { mutableStateOf("") }
    var customGitUrl by remember { mutableStateOf("") }

    // Configuration states
    var projectName by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("main") }
    var rootDirectory by remember { mutableStateOf("") }
    var selectedFrameworkId by remember { mutableStateOf<String?>("nextjs") }
    var frameworkSearchQuery by remember { mutableStateOf("") }
    var isFrameworkSheetOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Auto-fetch repos on opening if list is empty
    LaunchedEffect(Unit) {
        if (gitHubRepos.isEmpty()) {
            onFetchRepos(gitHubUsernameInput)
        }
    }

    val templates = listOf(
        StarterTemplate("ai-chatbot", "Next.js AI Chatbot", "Full-featured AI chat app with AI SDK & Tailwind.", "vercel/ai-chatbot", "nextjs", VercelBlueLight),
        StarterTemplate("next-app", "Next.js App Router", "Production-grade Next.js starter with Server Components.", "vercel/next.js", "nextjs", FrameworkNextJs),
        StarterTemplate("vite-react", "Vite + React SPA", "Blazing fast Single Page Application with React.", "vitejs/vite", "vite", FrameworkVite),
        StarterTemplate("astro-blog", "Astro Content Blog", "Content-focused, zero-JS by default blog template.", "withastro/astro", "astro", FrameworkAstro)
    )

    // Filtered GitHub repos
    val filteredRepos = remember(gitHubRepos, repoSearchQuery) {
        if (repoSearchQuery.isBlank()) {
            gitHubRepos
        } else {
            gitHubRepos.filter { r ->
                r.name.contains(repoSearchQuery, ignoreCase = true) ||
                (r.description?.contains(repoSearchQuery, ignoreCase = true) == true) ||
                (r.language?.contains(repoSearchQuery, ignoreCase = true) == true)
            }
        }
    }

    // Filtered frameworks
    val filteredFrameworks = remember(frameworkSearchQuery) {
        if (frameworkSearchQuery.isBlank()) {
            ALL_VERCEL_FRAMEWORKS
        } else {
            ALL_VERCEL_FRAMEWORKS.filter { f ->
                f.name.contains(frameworkSearchQuery, ignoreCase = true) ||
                f.category.contains(frameworkSearchQuery, ignoreCase = true)
            }
        }
    }

    val currentFramework = ALL_VERCEL_FRAMEWORKS.firstOrNull { it.id == selectedFrameworkId }
        ?: ALL_VERCEL_FRAMEWORKS.last()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VercelSurfaceElevated)
                    .border(1.dp, VercelBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .testTag("create_project_wizard_modal")
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (step == ProjectWizardStep.CONFIGURE_PROJECT) {
                            IconButton(
                                onClick = { step = ProjectWizardStep.SELECT_REPO },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = VercelWhitePure,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            VercelLogo(size = 20.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = if (step == ProjectWizardStep.SELECT_REPO) "Import Git Repository" else "Configure Project",
                            color = VercelWhitePure,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VercelGrayLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Official Vercel Git Integrations Link Banner
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VercelBlue.copy(alpha = 0.10f))
                        .border(1.dp, VercelBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://vercel.com/account/integrations")
                            )
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = VercelBlueLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connect GitHub/GitLab on vercel.com",
                        color = VercelBlueLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = VercelBlueLight,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (step == ProjectWizardStep.SELECT_REPO) {
                    // Source Type Tabs (GitHub Repos | Git URL | Templates)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VercelSurfaceVariant)
                            .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RepoSourceType.values().forEach { st ->
                            val isSelected = sourceType == st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) VercelWhitePure else Color.Transparent)
                                    .clickable { sourceType = st }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st.title,
                                    color = if (isSelected) VercelBlack else VercelGrayLight,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (sourceType) {
                        RepoSourceType.GITHUB_REPOS -> {
                            // GitHub Username Selector Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GeistTextField(
                                    value = gitHubUsernameInput,
                                    onValueChange = { gitHubUsernameInput = it },
                                    placeholder = "GitHub username (e.g. AbbasFullstack)",
                                    isMonospace = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = VercelGrayLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = { onFetchRepos(gitHubUsernameInput) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VercelWhitePure,
                                        contentColor = VercelBlack
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text("Fetch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            if (gitHubError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = gitHubError,
                                    color = StatusError,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Search inside fetched repos
                            GeistTextField(
                                value = repoSearchQuery,
                                onValueChange = { repoSearchQuery = it },
                                placeholder = "Search ${gitHubRepos.size} repositories…",
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = VercelGrayLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Repositories List
                            if (isFetchingRepos) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = VercelWhitePure,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else if (filteredRepos.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (gitHubRepos.isEmpty()) "No repositories found for @$gitHubUsernameInput" else "No matching repositories",
                                        color = VercelGrayLight,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredRepos, key = { it.id }) { repo ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(VercelSurfaceVariant)
                                                .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = repo.name,
                                                    color = VercelWhitePure,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (!repo.description.isNullOrBlank()) {
                                                    Text(
                                                        text = repo.description,
                                                        color = VercelGrayLight,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    if (!repo.language.isNullOrBlank()) {
                                                        Text(
                                                            text = repo.language,
                                                            color = VercelBlueLight,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                    Text(
                                                        text = "⭐ ${repo.stars}",
                                                        color = VercelGray,
                                                        fontSize = 10.sp
                                                    )
                                                    Text(
                                                        text = repo.defaultBranch,
                                                        color = VercelGray,
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // 1-Click Instant Import & Deploy
                                                Button(
                                                    onClick = {
                                                        val lowerName = repo.name.lowercase()
                                                        val lowerDesc = repo.description?.lowercase() ?: ""
                                                        val autoFw = when {
                                                            lowerName.contains("next") || lowerDesc.contains("next") -> "nextjs"
                                                            lowerName.contains("vite") || lowerDesc.contains("vite") -> "vite"
                                                            lowerName.contains("astro") || lowerDesc.contains("astro") -> "astro"
                                                            lowerName.contains("remix") || lowerDesc.contains("remix") -> "remix"
                                                            lowerName.contains("svelte") || lowerDesc.contains("svelte") -> "sveltekit"
                                                            lowerName.contains("nuxt") || lowerDesc.contains("nuxt") -> "nuxtjs"
                                                            lowerName.contains("react") || lowerDesc.contains("react") -> "create-react-app"
                                                            repo.language.equals("TypeScript", ignoreCase = true) || repo.language.equals("JavaScript", ignoreCase = true) -> "nextjs"
                                                            else -> null
                                                        }
                                                        val autoName = repo.name.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
                                                        onCreateProject(
                                                            autoName,
                                                            autoFw,
                                                            repo.fullName,
                                                            null,
                                                            repo.defaultBranch
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = VercelWhitePure,
                                                        contentColor = VercelBlack
                                                    ),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("⚡ 1-Click", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                // Configure Details
                                                OutlinedButton(
                                                    onClick = {
                                                        selectedRepoFullName = repo.fullName
                                                        projectName = repo.name.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
                                                        selectedBranch = repo.defaultBranch
                                                        step = ProjectWizardStep.CONFIGURE_PROJECT
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, VercelBorder),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Tune,
                                                        contentDescription = "Configure",
                                                        tint = VercelGrayLight,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        RepoSourceType.CUSTOM_URL -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "GIT REPOSITORY URL",
                                    color = VercelGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                GeistTextField(
                                    value = customGitUrl,
                                    onValueChange = { input ->
                                        customGitUrl = input
                                        val clean = input.trim()
                                            .removePrefix("https://github.com/")
                                            .removePrefix("http://github.com/")
                                            .removePrefix("github.com/")
                                            .removeSuffix(".git")
                                        val parts = clean.split("/")
                                        if (parts.size >= 2) {
                                            projectName = parts[1].lowercase().replace(" ", "-")
                                        }
                                    },
                                    placeholder = "https://github.com/username/repository or GitLab URL",
                                    isMonospace = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (customGitUrl.isNotBlank()) {
                                            selectedRepoFullName = customGitUrl.trim()
                                            step = ProjectWizardStep.CONFIGURE_PROJECT
                                        }
                                    },
                                    enabled = customGitUrl.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VercelWhitePure,
                                        contentColor = VercelBlack
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Continue with URL →", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        RepoSourceType.TEMPLATES -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(templates) { t ->
                                    GeistCard(
                                        onClick = {
                                            selectedRepoFullName = t.repo
                                            projectName = t.id
                                            selectedFrameworkId = t.framework
                                            step = ProjectWizardStep.CONFIGURE_PROJECT
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = t.title,
                                                color = VercelWhitePure,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = t.framework.uppercase(),
                                                color = t.badgeColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = t.description,
                                            color = VercelGrayLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Step 2: CONFIGURE_PROJECT
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Linked Repo Chip
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VercelSurfaceVariant)
                                    .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Source,
                                    contentDescription = null,
                                    tint = StatusReady,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedRepoFullName,
                                    color = VercelWhitePure,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "branch: $selectedBranch",
                                    color = VercelGrayLight,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Project Name
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "PROJECT NAME",
                                    color = VercelGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                GeistTextField(
                                    value = projectName,
                                    onValueChange = {
                                        projectName = it.lowercase().replace(" ", "-").filter { c ->
                                            c.isLetterOrDigit() || c == '-'
                                        }
                                    },
                                    placeholder = "my-vercel-project",
                                    isMonospace = true,
                                    testTag = "project_name_config_input"
                                )
                            }
                        }

                        // Framework Preset (Searchable Selection of ALL 37 frameworks)
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FRAMEWORK PRESET",
                                        color = VercelGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Select from 37 presets ↗",
                                        color = VercelBlueLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { isFrameworkSheetOpen = true }
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VercelSurfaceVariant)
                                        .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                                        .clickable { isFrameworkSheetOpen = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(currentFramework.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = currentFramework.name,
                                            color = VercelWhitePure,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = VercelGrayLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Root Directory
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "ROOT DIRECTORY (OPTIONAL)",
                                    color = VercelGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                GeistTextField(
                                    value = rootDirectory,
                                    onValueChange = { rootDirectory = it },
                                    placeholder = "./ (e.g. apps/web or frontend)",
                                    isMonospace = true
                                )
                            }
                        }

                        // Target Domain Pill
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VercelSurfaceVariant)
                                    .border(1.dp, VercelBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Target: ",
                                    color = VercelGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$projectName.vercel.app",
                                    color = VercelBlueLight,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Deploy Button
                    GeistButton(
                        text = "Deploy Project 🚀",
                        onClick = {
                            onCreateProject(
                                projectName.trim(),
                                selectedFrameworkId,
                                selectedRepoFullName.trim(),
                                if (rootDirectory.isNotBlank()) rootDirectory.trim() else null,
                                selectedBranch.trim()
                            )
                        },
                        enabled = projectName.isNotBlank(),
                        isLoading = isLoading,
                        style = GeistButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "submit_project_wizard_btn"
                    )
                }
            }
        }
    }

    // Full 37 Framework Presets Picker Sheet
    if (isFrameworkSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFrameworkSheetOpen = false },
            containerColor = VercelSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Select Framework Preset (37 Presets)",
                    color = VercelWhitePure,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                GeistTextField(
                    value = frameworkSearchQuery,
                    onValueChange = { frameworkSearchQuery = it },
                    placeholder = "Search framework (Next.js, Vite, Astro, Fasthtml…)",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = VercelGrayLight,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(filteredFrameworks) { fw ->
                        val isSelected = selectedFrameworkId == fw.id
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VercelSurfaceVariant else VercelSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) VercelWhitePure else VercelBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedFrameworkId = fw.id
                                    isFrameworkSheetOpen = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(fw.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = fw.name,
                                    color = if (isSelected) VercelWhitePure else VercelGrayLight,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = fw.category,
                                    color = VercelGray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
