package com.achmadichzan.rangkum.presentation.screen

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.presentation.components.ActionIcon
import com.achmadichzan.rangkum.presentation.components.MessageBubble
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChatScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onStartRecordingClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val userPrefDark by viewModel.isDarkMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkFinal = userPrefDark ?: systemDark
    val lastMessageText = viewModel.messages.lastOrNull()?.text ?: ""
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) true
            else {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 1
            }
        }
    }
    val showScrollToBottom by remember {
        derivedStateOf {
            !isAtBottom && viewModel.messages.isNotEmpty()
        }
    }
    var selectedMessage by remember { mutableStateOf<UiMessage?>(null) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val currentModel by viewModel.currentModel.collectAsState()
    val availableModels = viewModel.availableModels
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    val windowInfo = LocalWindowInfo.current
    val windowSize = windowInfo.containerSize
    val density = LocalDensity.current
    val screenWidthDp = with(density) { windowSize.width.toDp() }
    val screenHeightDp = with(density) { windowSize.height.toDp() }
    val isTablet = screenWidthDp >= 600.dp
    val isLandscape = screenWidthDp > screenHeightDp
    val showBackButton = !isLandscape && !isTablet

    LaunchedEffect(viewModel.messages.size, lastMessageText) {
        if (viewModel.messages.isNotEmpty()) {
            val lastIndex = viewModel.messages.size - 1
            if (viewModel.messages.last().isUser || isAtBottom) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    RangkumTheme(darkTheme = isDarkFinal) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = viewModel.sessionTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 17.sp
                            )
                            Text(
                                text = availableModels.find { it.first == currentModel }?.second ?: currentModel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Kembali"
                                )
                            }
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { isModelMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    "Ganti Model"
                                )
                            }

                            DropdownMenu(
                                expanded = isModelMenuExpanded,
                                onDismissRequest = { isModelMenuExpanded = false }
                            ) {
                                availableModels.forEach { (id, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(name)
                                                if (id == currentModel) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        Icons.Default.Check,
                                                        null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.onModelChange(id)
                                            isModelMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(modifier = Modifier.size(1.dp).focusable())
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 75.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) {
                    items(
                        viewModel.messages,
                        key = { it.id },
                        contentType = { if (it.isUser) 1 else 2 }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            onRetry = {
                                val currentIndex = viewModel.messages.indexOf(message)
                                if (currentIndex > 0) {
                                    val userPrompt = viewModel.messages[currentIndex - 1].text
                                    viewModel.regenerateResponse(userPrompt)
                                }
                            },
                            onEditSave = { newPrompt -> viewModel.regenerateResponse(newPrompt) },
                            onShowMenu = { selectedMessage = message }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (viewModel.isLoading) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI sedang berpikir...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showScrollToBottom,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 90.dp, end = 16.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (viewModel.messages.isNotEmpty()) {
                                    listState.animateScrollToItem(viewModel.messages.size - 1)
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Ke Bawah"
                        )
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onStartRecordingClick,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = "Rekam")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = viewModel.userInput,
                            onValueChange = { viewModel.onInputChange(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                if (viewModel.messages.isEmpty()) Text("Tanyakan sesuatu...")
                                else Text("Lanjut tanya AI...")
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.sendMessage() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Kirim")
                        }
                    }
                }

                if (viewModel.messages.isEmpty()) {
                    Text(
                        "Belum ada percakapan.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                AnimatedVisibility(
                    visible = selectedMessage != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    selectedMessage?.let { message ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ActionIcon(Icons.Default.ContentCopy, "Salin") {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "Rangkum",
                                                    message.text
                                                )
                                            )
                                        )
                                    }
                                    selectedMessage = null
                                }

                                if (message.isUser) {
                                    ActionIcon(Icons.Default.Edit, "Edit") {
                                        message.isEditing = true
                                        selectedMessage = null
                                    }
                                }

                                ActionIcon(Icons.Default.Close, "Tutup") { selectedMessage = null }
                            }
                        }
                    }
                }
            }
        }
    }
}