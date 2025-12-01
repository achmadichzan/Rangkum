package com.achmadichzan.rangkum.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.achmadichzan.rangkum.R
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.presentation.components.HistoryItem
import com.achmadichzan.rangkum.presentation.components.RenameDialog
import com.achmadichzan.rangkum.presentation.components.SearchBar
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme
import com.achmadichzan.rangkum.presentation.viewmodel.MainViewModel
import com.achmadichzan.rangkum.presentation.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onStartSession: (Long) -> Unit) {
    val factory = ViewModelFactory(LocalContext.current)
    val viewModel: MainViewModel = viewModel(factory = factory)
    val sessions by viewModel.allSessions.collectAsState()
    var showRenameDialog by remember { mutableStateOf(false) }
    var sessionToRename by remember { mutableStateOf<Session?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userPrefDark by viewModel.isDarkMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkFinal = userPrefDark ?: systemDark
    var showYoutubeDialog by remember { mutableStateOf(false) }
    var youtubeLink by remember { mutableStateOf("") }

    RangkumTheme(darkTheme = isDarkFinal) {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Rangkum AI", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { showYoutubeDialog = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.youtube),
                                    contentDescription = "Paste YouTube Link"
                                )
                            }

                            IconButton(onClick = { viewModel.toggleTheme(isDarkFinal) }) {
                                Icon(
                                    imageVector = if (isDarkFinal) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Ganti Tema"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    SearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        onStartSession(-1L)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Chat Baru")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    "Riwayat Rangkuman",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (sessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat chat.", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(sessions, key = { it.id }) { session ->
                            val dismissState = rememberSwipeToDismissBoxState()

                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteSession(session.id)
                                }
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                modifier = Modifier.animateItem(),
                                backgroundContent = {
                                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                HistoryItem(
                                    session = session,
                                    onClick = { onStartSession(session.id) },
                                    onEditClick = {
                                        sessionToRename = session
                                        showRenameDialog = true
                                    }
                                )
                            }

                            if (showRenameDialog && sessionToRename != null) {
                                RenameDialog(
                                    initialTitle = sessionToRename!!.title,
                                    onDismiss = { showRenameDialog = false },
                                    onConfirm = { newTitle ->
                                        if (newTitle.isNotBlank()) {
                                            viewModel.renameSession(sessionToRename!!.id, newTitle)
                                        }
                                        showRenameDialog = false
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        if (showYoutubeDialog) {
            AlertDialog(
                onDismissRequest = { showYoutubeDialog = false },
                title = { Text("Rangkum YouTube") },
                text = {
                    Column {
                        Text("Tempel link video YouTube untuk mengambil subtitlenya.")
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = youtubeLink,
                            onValueChange = { youtubeLink = it },
                            placeholder = { Text("https://youtu.be/...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (viewModel.youtubeError != null) {
                            Text(
                                text = viewModel.youtubeError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (youtubeLink.isNotBlank()) {
                                viewModel.processYoutubeLink(youtubeLink) { sessionId ->
                                    showYoutubeDialog = false
                                    youtubeLink = ""
                                    onStartSession(sessionId)
                                }
                            }
                        },
                        enabled = !viewModel.isYoutubeLoading
                    ) {
                        if (viewModel.isYoutubeLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Proses")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showYoutubeDialog = false
                            viewModel.clearError()
                        }
                    ) { Text("Batal") }
                }
            )
        }
    }
}