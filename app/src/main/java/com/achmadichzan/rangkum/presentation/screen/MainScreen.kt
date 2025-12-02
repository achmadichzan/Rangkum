package com.achmadichzan.rangkum.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf(false) }
    var sessionToRename by remember { mutableStateOf<Session?>(null) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val userPrefDark by viewModel.isDarkMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkFinal = userPrefDark ?: systemDark
    var showYoutubeDialog by remember { mutableStateOf(false) }
    var youtubeLink by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "BATAL",
                        duration = SnackbarDuration.Short
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }
            }
        }
    }

    RangkumTheme(darkTheme = isDarkFinal) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
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
                    ),
                    scrollBehavior = scrollBehavior,
                )
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
            },
            snackbarHost = { SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(8.dp),
                        action = {
                            data.visuals.actionLabel?.let { actionLabel ->
                                TextButton(
                                    onClick = { data.performAction() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.inversePrimary
                                    )
                                ) {
                                    Text(actionLabel)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = data.visuals.message,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                Box(modifier = Modifier.size(1.dp).focusable())
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Riwayat Rangkuman",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 11.dp)
                            )
                        }
                    }

                    if (sessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    Text("Tidak ditemukan: '$searchQuery'", color = Color.Gray)
                                } else {
                                    Text("Belum ada riwayat chat.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(sessions, key = { it.id }) { session ->
                            val dismissState = key(session.id) {
                                rememberSwipeToDismissBoxState(
                                    positionalThreshold = { totalDistance -> totalDistance * 0.8f },
                                )
                            }

                            LaunchedEffect(session) {
                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                modifier = Modifier.animateItem(),
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val color =
                                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
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
                                onDismiss = { direction ->
                                    if (direction == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteSession(session.id)
                                    }
                                }
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

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
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