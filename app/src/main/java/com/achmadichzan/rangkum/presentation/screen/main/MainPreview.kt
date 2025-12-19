package com.achmadichzan.rangkum.presentation.screen.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen_Empty() {
    RangkumTheme {
        MainScreenContent(
            sessions = emptyList(),
            searchQuery = "",
            isDarkTheme = false,
            voskModels = emptyList(),
            youtubeError = null,
            isYoutubeLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onStartSession = {},
            onSearchQueryChange = {},
            onToggleTheme = {},
            onDeleteSession = {},
            onRenameSession = { _, _ -> },
            onTogglePin = {},
            onProcessYoutubeLink = { _, _ -> },
            onClearYoutubeError = {},
            onDownloadModel = {},
            onSelectModel = {},
            onDeleteModel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen_WithData() {
    RangkumTheme {
        MainScreenContent(
            sessions = dummySessions,
            searchQuery = "",
            isDarkTheme = false,
            voskModels = emptyList(),
            youtubeError = null,
            isYoutubeLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onStartSession = {},
            onSearchQueryChange = {},
            onToggleTheme = {},
            onDeleteSession = {},
            onRenameSession = { _, _ -> },
            onTogglePin = {},
            onProcessYoutubeLink = { _, _ -> },
            onClearYoutubeError = {},
            onDownloadModel = {},
            onSelectModel = {},
            onDeleteModel = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Empty"
)
@Composable
fun PreviewMainScreen_Empty_Dark() {
    RangkumTheme(darkTheme = true) {
        MainScreenContent(
            sessions = emptyList(),
            searchQuery = "",
            isDarkTheme = true,
            voskModels = emptyList(),
            youtubeError = null,
            isYoutubeLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onStartSession = {},
            onSearchQueryChange = {},
            onToggleTheme = {},
            onDeleteSession = {},
            onRenameSession = { _, _ -> },
            onTogglePin = {},
            onProcessYoutubeLink = { _, _ -> },
            onClearYoutubeError = {},
            onDownloadModel = {},
            onSelectModel = {},
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
fun PreviewMainScreen_WithData_Dark() {
    RangkumTheme(darkTheme = true) {
        MainScreenContent(
            sessions = dummySessions,
            searchQuery = "",
            isDarkTheme = true,
            voskModels = emptyList(),
            youtubeError = null,
            isYoutubeLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onStartSession = {},
            onSearchQueryChange = {},
            onToggleTheme = {},
            onDeleteSession = {},
            onRenameSession = { _, _ -> },
            onTogglePin = {},
            onProcessYoutubeLink = { _, _ -> },
            onClearYoutubeError = {},
            onDownloadModel = {},
            onSelectModel = {},
            onDeleteModel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen_StaticSnackbar() {
    RangkumTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MainScreenContent(
                sessions = dummySessions,
                searchQuery = "",
                isDarkTheme = false,
                voskModels = emptyList(),
                youtubeError = null,
                isYoutubeLoading = false,
                snackbarHostState = remember { SnackbarHostState() },
                onStartSession = {},
                onSearchQueryChange = {},
                onToggleTheme = {},
                onDeleteSession = {},
                onRenameSession = { _, _ -> },
                onTogglePin = {},
                onProcessYoutubeLink = { _, _ -> },
                onClearYoutubeError = {},
                onDownloadModel = {},
                onSelectModel = {},
                onDeleteModel = {}
            )

            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(8.dp),
                action = {
                    TextButton(
                        onClick = {},
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.inversePrimary
                        )
                    ) {
                        Text("BATAL")
                    }
                }
            ) {
                Text(
                    text = "Sesi berhasil dihapus (Preview Mode)",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

val dummySessions = listOf(
    Session(
        id = 1,
        title = "Rangkuman Meeting Senin",
        timestamp = System.currentTimeMillis(),
        isPinned = true
    ),
    Session(
        id = 2,
        title = "Kuliah Sejarah Indonesia",
        timestamp = System.currentTimeMillis() - 86400000,
        isPinned = false
    ),
    Session(
        id = 3,
        title = "Tutorial Coding Android",
        timestamp = System.currentTimeMillis() - 172800000,
        isPinned = false
    )
)