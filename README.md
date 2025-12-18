# Rangkum AI 🤖📝

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=android)
![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%20Flash-orange?logo=google)
![Vosk](https://img.shields.io/badge/STT-Vosk%20Offline-green)
![Cloud Run](https://img.shields.io/badge/Backend-Google%20Cloud%20Run-blue?logo=googlecloud)

**Rangkum** is a powerful Android productivity tool designed to transcribe and summarize audio in real-time. It features a **Floating Overlay** that captures internal system audio (Zoom, Meet, Youtube, etc) and uses **Offline Speech-to-Text** combined with **Generative AI** to create concise summaries instantly.

It also supports **YouTube Link Summarization** via a custom Python backend.

---

## 📱 Screenshots

| Home Screen | Floating Overlay | Summary | Tablet/Adaptive |
|:-----------:|:----------------:|:-----------------:|:---------------:|
| <img src="https://github.com/user-attachments/assets/f17510cf-1f1f-42c1-894a-b2cca8a5b4fb" width="200"/> | <img src="https://github.com/user-attachments/assets/bdda5587-4ebd-4068-8d27-b664f0bcaa60" width="200"/> | <img src="https://github.com/user-attachments/assets/448a7f8a-807b-4647-84d5-812526abc2f8" width="200"/> | <img src="https://github.com/user-attachments/assets/de71e110-ab72-4a69-9e02-d6fb26dae814" width="200"/> |

---

## ✨ Key Features

* **🎧 Internal Audio Recording (Overlay):**
    * Floating bubble interface (Picture-in-Picture style).
    * Captures system audio (Android 10+) directly without microphone noise.
    * **Resizible & Collapsible** window.
* **🗣️ Multi-Language Offline STT:**
    * **Dynamic Model Management:** Support for **30+ languages** (English, Chinese, Japanese, etc.).
    * **On-Demand Downloads:** Download only the languages you need to keep the app size small.
    * **Smart Downloader:** Features **Resumable Downloads** (handles network interruptions) and memory-efficient streaming using Ktor.
    * Privacy-focused: All audio processing happens on-device using **Vosk**.
* **🧠 AI Summarization:**
    * Integrated with **Google Gemini 2.5 Flash, Gemini 2.5 Flash Lite, Gemini 2.5 Pro (Paid)**.
    * **Streaming Response:** Text appears word-by-word (typewriter effect) for zero-latency feel.
    * Custom Markdown rendering for clean output.
* **📺 YouTube Summarizer:**
    * Paste any YouTube link to get a summary.
    * Powered by a custom **Python Backend (Flask + yt-dlp)** hosted on Google Cloud Run.
    * Auto-detects video language & handles cookies for anti-blocking.
* **💾 Smart History:**
    * Save chats locally using **Room Database**.
    * Swipe-to-delete with Undo (Snackbar) capability.
    * Search & Rename functionality.
* **🎨 Modern UI/UX:**
    * **Material 3 Design** with Dark/Light theme support.
    * **Adaptive Layout:** Supports Foldables & Tablets using `NavigableListDetailPaneScaffold`.
    * **Interactive Chat:** Edit prompts, retry generation, and copy text blocks.

---

## 🛠️ Tech Stack

### Android (Client)
* **Language:** Kotlin
* **Architecture:** Clean Architecture (MVVM + Repository Pattern)
* **UI:** Jetpack Compose, Material 3 Adaptive
* **Dependency Injection:** Koin
* **Concurrency:** Coroutines & Flow
* **Network:** Ktor Client (Backend API & **Resumable File Downloader**)
* **Database:** Room (SQLite)
* **Local Storage:** DataStore Preferences
* **AI SDK:** Firebase AI (Gemini)
* **Audio:** AudioRecord API, MediaProjection API
* **STT:** Vosk Android Library

### Backend (Microservice)
* **Language:** Python 3.13.9
* **Framework:** Flask
* **Tools:** `yt-dlp` (YouTube Downloader), `gunicorn`
* **Infrastructure:** Google Cloud Run (Serverless)
* **Storage:** Google Cloud Storage (for cookie management)

---

## 🏗️ Architecture

The project follows strictly **Clean Architecture** principles to ensure scalability and testability.

```
com.achmadichzan.rangkum
├── data
│   └── # Implementation of Repositories, Data Sources (Room, Ktor, Gemini)
├── domain
│   └── # Business Logic (Use Cases, Repository Interfaces, Models)
├── presentation
│   └── # UI (Screens, ViewModels, Components, Service)
└── di
    └── # Dependency Injection Modules
```

**Key Highlights:**
* **OverlayService:** Handles the complexity of `WindowManager`, `MediaProjection`, and background audio processing.
* **ModelRepository:** Implements a robust **Resumable Download** logic using HTTP `Range` headers to handle large zip files and prevent OOM (Out of Memory) errors during extraction.
* **AudioTranscriber:** A dedicated class for managing the audio loop, Vosk inference, and thread synchronization (Mutex).

---

## 🚀 Getting Started

### Prerequisites
1.  Android Studio Otter or newer.
2.  Device running **Android 10 (API 29)** or higher (Required for internal audio capture).
3.  Firebase Project with **AI** enabled.

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/achmadichzan/Rangkum.git](https://github.com/achmadichzan/Rangkum.git)
    ```

2.  **Setup Firebase:**
    * Create a project in Firebase Console.
    * Download `google-services.json`.
    * Place it in the `app/` directory.

3.  **Build & Run:**
    * Sync Gradle.
    * Run on a physical device (Emulators often fail with Audio Capture).

---

## 🛡️ Permissions

The app requires sensitive permissions due to its nature:
* `RECORD_AUDIO`: To capture voice/system sound.
* `FOREGROUND_SERVICE_MEDIA_PROJECTION`: To keep the overlay alive and capture audio.
* `SYSTEM_ALERT_WINDOW`: To draw the overlay over other apps.
* `INTERNET`: For Gemini AI and YouTube Backend.

---

## 🤝 Contributing

Contributions are welcome! Please fork the repository and create a pull request for any bug fixes or feature additions.

---

**Built by Achmad Ichzan & Gemini 3 Pro**
