package com.achmadichzan.rangkum.presentation.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achmadichzan.rangkum.R
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.model.UiVoskModel
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.presentation.components.HistoryItem
import com.achmadichzan.rangkum.presentation.components.LanguageSelectionDialog
import com.achmadichzan.rangkum.presentation.components.RenameDialog
import com.achmadichzan.rangkum.presentation.components.SearchBar
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import com.achmadichzan.rangkum.presentation.viewmodels.MainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    chatViewModel: ChatViewModel = koinViewModel(),
    onStartSession: (Long) -> Unit
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val userPrefDark by viewModel.isDarkMode.collectAsState()
    val voskModels by chatViewModel.voskModels.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkFinal = userPrefDark ?: systemDark
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

    MainScreenContent(
        sessions = sessions,
        searchQuery = searchQuery,
        isDarkTheme = isDarkFinal,
        voskModels = voskModels,
        youtubeError = viewModel.youtubeError,
        isYoutubeLoading = viewModel.isYoutubeLoading,
        snackbarHostState = snackbarHostState,
        onStartSession = onStartSession,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleTheme = { viewModel.toggleTheme(isDarkFinal) },
        onDeleteSession = { viewModel.deleteSession(it) },
        onRenameSession = { id, title -> viewModel.renameSession(id, title) },
        onTogglePin = { viewModel.togglePin(it) },
        onProcessYoutubeLink = { link, onComplete ->
            viewModel.processYoutubeLink(link, onComplete)
        },
        onClearYoutubeError = { viewModel.clearError() },
        onDownloadModel = { chatViewModel.downloadModel(it) },
        onSelectModel = { chatViewModel.selectVoskModel(it) },
        onDeleteModel = { chatViewModel.deleteModel(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    sessions: List<Session>,
    searchQuery: String,
    isDarkTheme: Boolean,
    voskModels: List<UiVoskModel>,
    youtubeError: String?,
    isYoutubeLoading: Boolean,
    snackbarHostState: SnackbarHostState,

    onStartSession: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onRenameSession: (Long, String) -> Unit,
    onTogglePin: (Session) -> Unit,

    onProcessYoutubeLink: (String, (Long) -> Unit) -> Unit,
    onClearYoutubeError: () -> Unit,

    onDownloadModel: (VoskModelConfig) -> Unit,
    onSelectModel: (VoskModelConfig) -> Unit,
    onDeleteModel: (VoskModelConfig) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var sessionToRename by remember { mutableStateOf<Session?>(null) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showNewChatLanguageDialog by remember { mutableStateOf(false) }
    var showYoutubeDialog by remember { mutableStateOf(false) }
    var youtubeLink by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    val pinnedCount = remember(sessions) { sessions.count { it.isPinned } }
    val isPinLimitReached = pinnedCount >= 3

    LaunchedEffect(listState) {
        var prevIndex = 0
        var prevOffset = 0

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (currIndex, currOffset) ->
                if (listState.isScrollInProgress) {
                    if (currIndex != prevIndex || currOffset != prevOffset) {
                        val isScrollingDown = if (currIndex != prevIndex) {
                            currIndex > prevIndex
                        } else {
                            currOffset > prevOffset
                        }

                        isFabVisible = !isScrollingDown || currIndex == 0
                    }
                }
                else if (currIndex == 0) {
                    isFabVisible = true
                }

                prevIndex = currIndex
                prevOffset = currOffset
            }
    }

    RangkumTheme(darkTheme = isDarkTheme) {
        if (showNewChatLanguageDialog) {
            LanguageSelectionDialog(
                models = voskModels,
                onDismiss = { showNewChatLanguageDialog = false },
                onDownload = onDownloadModel,
                onSelect = onSelectModel,
                onDelete = onDeleteModel,
                onConfirm = { onStartSession(-1L) }
            )
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Rangkum AI", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector =
                                    if (isDarkTheme) Icons.Default.LightMode
                                    else Icons.Default.DarkMode,
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
                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = { showYoutubeDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.youtube),
                                contentDescription = "Paste YouTube Link"
                            )
                        }

                        FloatingActionButton(
                            onClick = { showNewChatLanguageDialog = true},
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Add, "Chat Baru")
                        }
                    }
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
                } }
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
                    onQueryChange = onSearchQueryChange
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp)
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
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance -> totalDistance * 0.8f }
                            )

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
                                        onDeleteSession(session.id)
                                    }
                                }
                            ) {
                                HistoryItem(
                                    session = session,
                                    isPinLimitReached = isPinLimitReached,
                                    onClick = { onStartSession(session.id) },
                                    onRenameClick = {
                                        sessionToRename = session
                                        showRenameDialog = true
                                    },
                                    onPinClick = {
                                        onTogglePin(session)
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
                        onRenameSession(sessionToRename!!.id, newTitle)
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

                        if (youtubeError != null) {
                            Text(
                                text = youtubeError,
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
                                onProcessYoutubeLink(youtubeLink) { sessionId ->
                                    showYoutubeDialog = false
                                    youtubeLink = ""
                                    onStartSession(sessionId)
                                }
                            }
                        },
                        enabled = !isYoutubeLoading
                    ) {
                        if (isYoutubeLoading) {
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
                            onClearYoutubeError()
                        }
                    ) { Text("Batal") }
                }
            )
        }
    }
}