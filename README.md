<div align="center">

# ▲ Vercel Mobile — Native Android Client

**A high-performance, zero-lag native Android client for Vercel, built with Jetpack Compose & Geist Design System.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-brightgreen.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Vercel API](https://img.shields.io/badge/Vercel%20REST%20API-v2%20--%20v13-black.svg?logo=vercel)](https://vercel.com/docs/rest-api)
[![Room Database](https://img.shields.io/badge/AndroidX%20Room-2.7-orange.svg)](https://developer.android.com/training/data-storage/room)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

*Crafted with precision by **Abbas Hussain** (Full Stack Developer & Engineer)*

---

</div>

## 💡 Why This Exists

As full-stack developers deploying multiple projects to Vercel daily, opening a mobile browser to check build statuses or trigger deployments is often slow, heavy, and frustrating on mobile connections. 

**Vercel Mobile** solves this with a native Android experience:
- **0ms Initial Load Time**: Instant cache-first rendering via Room DB.
- **One-Tap Actions**: Trigger redeployments (with or without build cache) in seconds.
- **Live Build Logs**: Real-time terminal console output for ongoing builds.
- **Authentic Geist Aesthetics**: Pitch-black `#000000` monochrome theme, monospace code elements, and pulsing status pills.

---

## 🚀 Key Features

### 🧙‍♂️ 1. 3-Step Onboarding Wizard
- **Step 1: Get Token Guide** — Direct in-app guidance on generating a personal access token with a 1-tap browser shortcut to `vercel.com/account/tokens`.
- **Step 2: Auto-Detection & Verification** — Automatically detects tokens from clipboard with instant paste, masked visibility, and real-time API verification.
- **Step 3: Profile Confirmation** — Shows verified avatar, username, email, and detected teams before launching into the dashboard.

### ⚡ 2. 0-Lag Cache-First Dashboard
- **Instant Rendering**: Projects and status badges render with zero network delay from local Room SQLite cache.
- **Non-blocking Sync**: Background sync seamlessly refreshes deployments without blanking or freezing the UI.
- **Live Search & Filter**: Instant filtering by project name, GitHub repo, or framework preset (Next.js, Vite, React, Astro, Remix, Svelte, Nuxt).

### 🔄 3. Production Redeployment Controls
- **Redeploy with Cache**: Rapid deployment reusing existing build artifacts.
- **Clean Redeploy (without Cache)**: Fresh scratch build for debugging dependency issues.
- **Cancel Build**: 1-tap cancellation for queued or runaway builds.

### 📜 4. Terminal-Style Live Build Logs
- Dark console viewer with line numbering and stdout/stderr color coding.
- Directly streams event lines from `GET /v2/deployments/{id}/events`.
- Instant manual reload and copy buttons.

### 🏢 5. Multi-Scope & Team Management
- Seamlessly switch between your **Personal Account** and any **Vercel Team**.
- All queries (projects, deployments, domains) automatically update their scope.

### 🛠️ 6. New Project Creation Wizard
- 3-step wizard to initialize and deploy new projects directly from mobile.
- Presets for Next.js, Vite, React, Astro, Remix, SvelteKit, and Nuxt with live domain previews (`my-app.vercel.app`).

### 🌐 7. Custom Domains & Environment Variables
- Inspect SSL verification status for all assigned custom domains.
- Add new custom domains directly from mobile.
- Reveal/hide environment variables across Production, Preview, and Development targets.

---

## 🛠️ Architecture & Tech Stack

This project follows modern Android architecture guidelines (Clean MVVM + Repository pattern):

```
app/src/main/java/com/example/
├── data/
│   ├── api/             # Retrofit 2 interface, OkHttp 3 & Auth Interceptor
│   ├── local/           # Room Database, DAOs & Entities (Tokens, Cache)
│   ├── model/           # Moshi data models for Vercel REST APIs
│   └── repository/      # Repository coordinating Room Cache + Vercel Remote API
├── ui/
│   ├── components/      # Geist Cards, Buttons, StatusBadges, VercelLogo
│   ├── screens/
│   │   ├── auth/        # 3-Step Token Setup Wizard
│   │   ├── dashboard/   # BottomNav (Projects, Deployments, Deploy Hub, Account)
│   │   ├── project/     # Project Details, Domains, Env Vars
│   │   ├── deployment/  # Deployment inspection & Live Terminal Logs
│   │   └── newproject/  # 3-Step Project Creation Wizard
│   └── theme/           # Geist monochrome Dark Theme & Tokens
├── MainActivity.kt      # Main Entry Point with type-safe state transitions
└── MainViewModel.kt     # StateFlow & Coroutines state management
```

| Technology | Purpose |
|---|---|
| **Kotlin 2.2** | Modern, concise, type-safe programming |
| **Jetpack Compose (M3)** | Declarative, smooth 120 FPS UI |
| **AndroidX Room (KSP)** | High-speed local persistence for zero lag |
| **Retrofit 2 & Moshi** | Robust Vercel REST API communication |
| **OkHttp 3** | Resilient HTTP networking with dynamic Bearer auth |
| **Coil Compose** | Memory & disk-cached image and avatar loading |
| **Coroutines & StateFlow** | Reactive, asynchronous background execution |

---

## 📦 How to Build & Run

### Prerequisites
- Android Studio Hedgehog (or newer)
- Android SDK 34+
- Java 11 or 17

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/abbaspowered/vercel-android-client.git
   cd vercel-android-client
   ```

2. **Open in Android Studio:**
   - Open Android Studio and select `File > Open`, then choose the cloned folder.
   - Let Gradle sync dependencies.

3. **Build and Run:**
   - Connect an Android device or launch an emulator.
   - Click **Run (Shift + F10)** or build the APK via:
     ```bash
     ./gradlew assembleDebug
     ```
   - APK output location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔐 Security & Privacy Note

- **Zero Cloud Middleman**: All API calls are made directly between your Android device and `https://api.vercel.com/`.
- **Local Storage**: Your Vercel Personal Access Token is stored exclusively in your device's private SQLite database managed by Room.
- **No Analytics / Telemetry**: No third-party tracking, advertising, or data collection SDKs.

---

## 👨‍💻 Author

**Abbas Hussain**
- **Role**: Full Stack Developer & Mobile Software Engineer
- **Email**: [abbaspowered@gmail.com](mailto:abbaspowered@gmail.com)
- **GitHub**: [@abbaspowered](https://github.com/abbaspowered)

*If this project helped you or you are interested in collaborating, feel free to reach out or drop a star!* ⭐️

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free for personal and commercial use.

*Disclaimer: This is an independent open-source client and is not officially affiliated with or endorsed by Vercel Inc.*
