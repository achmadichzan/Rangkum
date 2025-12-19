package com.achmadichzan.rangkum.presentation.screen.detail

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.domain.model.UiVoskModel
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.presentation.components.ActionIcon
import com.achmadichzan.rangkum.presentation.components.LanguageSelectionDialog
import com.achmadichzan.rangkum.presentation.components.MessageBubble
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun DetailChatScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onStartRecordingClick: () -> Unit
) {
    val voskModels by viewModel.voskModels.collectAsState()
    val userPrefDark by viewModel.isDarkMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkFinal = userPrefDark ?: systemDark

    val currentModelId by viewModel.currentModel.collectAsState()
    val availableModels = viewModel.availableModels
    val currentModelName = availableModels.find { it.first == currentModelId }
        ?.second ?: currentModelId

    val activeLang = voskModels.find { it.status == ModelStatus.ACTIVE }
        ?.config?.name ?: ""

    DetailChatContent(
        sessionTitle = viewModel.sessionTitle,
        currentModelName = currentModelName,
        activeLanguage = activeLang,
        messages = viewModel.messages,
        voskModels = voskModels,
        availableModels = availableModels,
        currentModelId = currentModelId,
        userInput = viewModel.userInput,
        isLoading = viewModel.isLoading,
        isDarkTheme = isDarkFinal,
        onBackClick = onBackClick,
        onStartRecordingClick = onStartRecordingClick,
        onInputChange = { viewModel.onInputChange(it) },
        onSendMessage = { viewModel.sendMessage() },
        onRegenerateResponse = { viewModel.regenerateResponse(it) },
        onModelChange = { viewModel.onModelChange(it) },
        onDownloadModel = { viewModel.downloadModel(it) },
        onSelectVoskModel = { viewModel.selectVoskModel(it) },
        onDeleteModel = { viewModel.deleteModel(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChatContent(
    sessionTitle: String,
    currentModelName: String,
    activeLanguage: String,
    messages: List<UiMessage>,
    voskModels: List<UiVoskModel>,
    availableModels: List<Pair<String, String>>,
    currentModelId: String,
    userInput: String,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    onBackClick: () -> Unit,
    onStartRecordingClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRegenerateResponse: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onDownloadModel: (VoskModelConfig) -> Unit,
    onSelectVoskModel: (VoskModelConfig) -> Unit,
    onDeleteModel: (VoskModelConfig) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val lastMessageText = messages.lastOrNull()?.text ?: ""
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
            !isAtBottom && messages.isNotEmpty()
        }
    }
    var selectedMessage by remember { mutableStateOf<UiMessage?>(null) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    val windowInfo = LocalWindowInfo.current
    val windowSize = windowInfo.containerSize
    val density = LocalDensity.current
    val screenWidthDp = with(density) { windowSize.width.toDp() }
    val screenHeightDp = with(density) { windowSize.height.toDp() }
    val isTablet = screenWidthDp >= 600.dp
    val isLandscape = screenWidthDp > screenHeightDp
    val showBackButton = !isLandscape && !isTablet

    LaunchedEffect(messages.size, lastMessageText) {
        if (messages.isNotEmpty()) {
            val lastIndex = messages.size - 1
            if (messages.last().isUser || isAtBottom) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    RangkumTheme(darkTheme = isDarkTheme) {
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                models = voskModels,
                onDismiss = { showLanguageDialog = false },
                onDownload = onDownloadModel,
                onSelect = onSelectVoskModel,
                onDelete = onDeleteModel,
                onConfirm = null
            )
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = sessionTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 17.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentModelName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (activeLanguage.isNotEmpty()) {
                                    Text(
                                        " - $activeLanguage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
                        Row {
                            IconButton(onClick = { showLanguageDialog = true }) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = "Ganti Bahasa"
                                )
                            }
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
                                                    if (id == currentModelId) {
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
                                                onModelChange(id)
                                                isModelMenuExpanded = false
                                            }
                                        )
                                    }
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
                Box(modifier = Modifier
                    .size(1.dp)
                    .focusable())
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
                        messages,
                        key = { it.id },
                        contentType = { if (it.isUser) 1 else 2 }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            onRetry = {
                                val currentIndex = messages.indexOf(message)
                                if (currentIndex > 0) {
                                    val userPrompt = messages[currentIndex - 1].text
                                    onRegenerateResponse(userPrompt)
                                }
                            },
                            onEditSave = { newPrompt -> onRegenerateResponse(newPrompt) },
                            onShowMenu = { selectedMessage = message }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isLoading) {
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
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
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

//                        OutlinedTextField(
//                            value = userInput,
//                            onValueChange = onInputChange,
//                            modifier = Modifier.weight(1f),
//                            placeholder = {
//                                if (messages.isEmpty()) Text("Tanyakan sesuatu...")
//                                else Text("Lanjut tanya AI...")
//                            },
//                            maxLines = 3,
//                            shape = RoundedCornerShape(24.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedContainerColor = MaterialTheme.colorScheme.surface,
//                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
//                            )
//                        )
                        var isFocused by remember { mutableStateOf(false) }
                        BasicTextField(
                            value = userInput,
                            onValueChange = { onInputChange(it) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(43.dp, 200.dp)
                                .onFocusChanged { isFocused = it.isFocused }
                                .border(
                                    width = 1.dp,
                                    color = if (isFocused) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 4,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (messages.isEmpty() && userInput.isEmpty()) {
                                        Text(
                                            text = "Tanyakan sesuatu...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else if (messages.isNotEmpty() && userInput.isEmpty()) {
                                        Text(
                                            text = "Lanjut tanya AI...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onSendMessage,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(22.dp)
                                    .offset(x = 2.dp),
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim"
                            )
                        }
                    }
                }

                if (messages.isEmpty()) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
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