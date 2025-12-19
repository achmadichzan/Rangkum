package com.achmadichzan.rangkum.presentation.screen.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.domain.model.UiVoskModel
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme

@Preview(
    showBackground = true,
    backgroundColor = 0x00000000,
    heightDp = 500,
    widthDp = 400
)
@Composable
fun PreviewOverlayChat_Standby() {
    val dummyMessages = listOf(
        UiMessage(
            id = "1",
            initialText = "Halo, ada yang bisa saya bantu?",
            isUser = false
        ),
        UiMessage(
            id = "2",
            initialText = "Coba rangkum meeting ini.",
            isUser = true
        ),
        UiMessage(
            id = "3",
            initialText = "Tentu, silakan mulai merekam audio untuk saya rangkum.",
            isUser = false
        )
    )

    RangkumTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            OverlayChatContent(
                messages = dummyMessages,
                voskModels = emptyList(),
                liveTranscript = "",
                userInput = "",
                isLoading = false,
                isPreparing = false,
                isRecording = false,
                isPaused = false,
                isCollapsed = false,
                onStartRecording = {},
                onStopRecording = {},
                onTogglePause = {},
                onCancelRecording = {},
                onCloseApp = {},
                onWindowDrag = { _, _ -> },
                onWindowResize = { _, _ -> },
                onResetTranscript = {},
                onUpdateTranscript = {},
                onToggleCollapse = {},
                onOpacityChange = {},
                onInputChange = {},
                onSendMessage = {},
                onRegenerateResponse = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOverlayChat_Recording() {
    val modelVosk = listOf(
        UiVoskModel(
            config = VoskModelConfig(
                code = "en",
                name = "English (Small)",
                url = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
                folderName = "vosk-model-small-en-us-0.15",
                size = "40MB"
            ),
            status = ModelStatus.ACTIVE
        )
    )
    RangkumTheme {
        OverlayChatContent(
            messages = emptyList(),
            voskModels = modelVosk,
            liveTranscript = "Ini adalah contoh teks transkrip yang sedang berjalan secara live...",
            userInput = "",
            isLoading = false,
            isPreparing = false,
            isRecording = true,
            isPaused = false,
            isCollapsed = false,
            onStartRecording = {},
            onStopRecording = {},
            onTogglePause = {},
            onCancelRecording = {},
            onCloseApp = {},
            onWindowDrag = { _, _ -> },
            onWindowResize = { _, _ -> },
            onResetTranscript = {},
            onUpdateTranscript = {},
            onToggleCollapse = {},
            onOpacityChange = {},
            onInputChange = {},
            onSendMessage = {},
            onRegenerateResponse = {}
        )
    }
}

@Preview(widthDp = 56, heightDp = 56)
@Composable
fun PreviewOverlayChat_Collapsed_Recording() {
    RangkumTheme {
        OverlayChatContent(
            messages = emptyList(),
            voskModels = emptyList(),
            liveTranscript = "",
            userInput = "",
            isLoading = false,
            isPreparing = false,
            isRecording = true,
            isPaused = false,
            isCollapsed = true,
            onStartRecording = {},
            onStopRecording = {},
            onTogglePause = {},
            onCancelRecording = {},
            onCloseApp = {},
            onWindowDrag = { _, _ -> },
            onWindowResize = { _, _ -> },
            onResetTranscript = {},
            onUpdateTranscript = {},
            onToggleCollapse = {},
            onOpacityChange = {},
            onInputChange = {},
            onSendMessage = {},
            onRegenerateResponse = {}
        )
    }
}