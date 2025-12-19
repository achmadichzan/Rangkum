package com.achmadichzan.rangkum.presentation.screen.detail

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme

@Preview(showBackground = true)
@Composable
fun PreviewDetailChat_WithData() {
    RangkumTheme {
        DetailChatContent(
            sessionTitle = "Belajar Kotlin",
            currentModelName = "Flash 2.5 (Standar)",
            activeLanguage = "Indonesia (Small)",
            messages = dummyMessages,
            voskModels = emptyList(),
            availableModels = listOf("gemini-2.5-flash" to "Flash 2.5 (Pintar)", "gemini-2.5-pro" to "Pro 2.5 (Pintar)"),
            currentModelId = "gemini-2.5-flash",
            userInput = "",
            isLoading = false,
            isDarkTheme = false,
            onBackClick = {},
            onStartRecordingClick = {},
            onInputChange = {},
            onSendMessage = {},
            onRegenerateResponse = {},
            onModelChange = {},
            onDownloadModel = {},
            onSelectVoskModel = {},
            onDeleteModel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDetailChat_Loading() {
    RangkumTheme {
        DetailChatContent(
            sessionTitle = "Live Transkrip",
            currentModelName = "Pro 2.5 (Pintar)",
            activeLanguage = "English (Small)",
            messages = listOf(
                UiMessage(
                    id = "1",
                    initialText = "Menganalisis audio...",
                    isUser = true
                )
            ),
            voskModels = emptyList(),
            availableModels = emptyList(),
            currentModelId = "gemini-2.5-pro",
            userInput = "Tolong tambahkan detail...",
            isLoading = true,
            isDarkTheme = false,
            onBackClick = {},
            onStartRecordingClick = {},
            onInputChange = {},
            onSendMessage = {},
            onRegenerateResponse = {},
            onModelChange = {},
            onDownloadModel = {},
            onSelectVoskModel = {},
            onDeleteModel = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - With Data"
)
@Composable
fun PreviewDetailChat_WithData_Dark() {
        RangkumTheme(darkTheme = true) {
        DetailChatContent(
            sessionTitle = "Belajar Kotlin",
            currentModelName = "Flash 2.5 (Standar)",
            activeLanguage = "Indonesia (Small)",
            messages = dummyMessages,
            voskModels = emptyList(),
            availableModels = listOf("gemini-2.5-flash" to "Flash 2.5 (Pintar)", "gemini-2.5-pro" to "Pro 2.5 (Pintar)"),
            currentModelId = "gemini-2.5-flash",
            userInput = "",
            isLoading = false,
            isDarkTheme = true, // Set true agar Logic internal menyesuaikan
            onBackClick = {},
            onStartRecordingClick = {},
            onInputChange = {},
            onSendMessage = {},
            onRegenerateResponse = {},
            onModelChange = {},
            onDownloadModel = {},
            onSelectVoskModel = {},
            onDeleteModel = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Loading"
)
@Composable
fun PreviewDetailChat_Loading_Dark() {
    RangkumTheme(darkTheme = true) {
        DetailChatContent(
            sessionTitle = "Live Transkrip",
            currentModelName = "Pro 2.5 (Pintar)",
            activeLanguage = "English (Small)",
            messages = listOf(
                UiMessage(
                    id = "1",
                    initialText = "Menganalisis audio...",
                    isUser = true
                )
            ),
            voskModels = emptyList(),
            availableModels = emptyList(),
            currentModelId = "gemini-2.5-pro",
            userInput = "Tolong tambahkan detail...",
            isLoading = true,
            isDarkTheme = true,
            onBackClick = {},
            onStartRecordingClick = {},
            onInputChange = {},
            onSendMessage = {},
            onRegenerateResponse = {},
            onModelChange = {},
            onDownloadModel = {},
            onSelectVoskModel = {},
            onDeleteModel = {}
        )
    }
}

val dummyMessages = listOf(
    UiMessage(
        id = "1",
        initialText = "Halo, tolong jelaskan tentang Kotlin Coroutines.",
        isUser = true
    ),
    UiMessage(
        id = "2",
        initialText = "Kotlin Coroutines adalah pattern desain untuk konkurensi...",
        isUser = false
    ),
    UiMessage(id = "3", initialText = "Apakah lebih baik dari Thread?", isUser = true)
)