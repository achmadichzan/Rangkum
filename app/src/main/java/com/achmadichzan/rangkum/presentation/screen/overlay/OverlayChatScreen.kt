package com.achmadichzan.rangkum.presentation.screen.overlay

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.domain.model.UiVoskModel
import com.achmadichzan.rangkum.presentation.components.ActionIcon
import com.achmadichzan.rangkum.presentation.components.MessageBubble
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun OverlayChatScreen(
    viewModel: ChatViewModel,
    isPreparing: Boolean,
    isRecording: Boolean,
    isPaused: Boolean,
    isCollapsed: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTogglePause: () -> Unit,
    onCancelRecording: () -> Unit,
    onCloseApp: () -> Unit,
    onWindowDrag: (Float, Float) -> Unit,
    onWindowResize: (Float, Float) -> Unit,
    onResetTranscript: () -> Unit,
    onUpdateTranscript: (String) -> Unit,
    onToggleCollapse: () -> Unit,
    onOpacityChange: (Float) -> Unit,
) {
    val voskModels by viewModel.voskModels.collectAsState()

    OverlayChatContent(
        messages = viewModel.messages,
        voskModels = voskModels,
        liveTranscript = viewModel.liveTranscript,
        userInput = viewModel.userInput,
        isLoading = viewModel.isLoading,
        isPreparing = isPreparing,
        isRecording = isRecording,
        isPaused = isPaused,
        isCollapsed = isCollapsed,
        onStartRecording = onStartRecording,
        onStopRecording = onStopRecording,
        onTogglePause = onTogglePause,
        onCancelRecording = onCancelRecording,
        onCloseApp = onCloseApp,
        onWindowDrag = onWindowDrag,
        onWindowResize = onWindowResize,
        onResetTranscript = onResetTranscript,
        onUpdateTranscript = onUpdateTranscript,
        onToggleCollapse = onToggleCollapse,
        onOpacityChange = onOpacityChange,
        onInputChange = { viewModel.onInputChange(it) },
        onSendMessage = { viewModel.sendMessage() },
        onRegenerateResponse = { viewModel.regenerateResponse(it) }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayChatContent(
    messages: List<UiMessage>,
    voskModels: List<UiVoskModel>,
    liveTranscript: String,
    userInput: String,
    isLoading: Boolean,
    isPreparing: Boolean,
    isRecording: Boolean,
    isPaused: Boolean,
    isCollapsed: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTogglePause: () -> Unit,
    onCancelRecording: () -> Unit,
    onCloseApp: () -> Unit,
    onWindowDrag: (Float, Float) -> Unit,
    onWindowResize: (Float, Float) -> Unit,
    onResetTranscript: () -> Unit,
    onUpdateTranscript: (String) -> Unit,
    onToggleCollapse: () -> Unit,
    onOpacityChange: (Float) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRegenerateResponse: (String) -> Unit
) {
    val activeModelName = remember(voskModels) {
        voskModels.find { it.status == ModelStatus.ACTIVE }?.config?.name
    }
    val listState = rememberLazyListState()
    val lastMessage = messages.lastOrNull()
    val lastMessageText = lastMessage?.text ?: ""
    val lastMessageIsStreaming = lastMessage?.isStreaming ?: false
    var selectedMessage by remember { mutableStateOf<UiMessage?>(null) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var currentAlpha by remember { mutableFloatStateOf(1f) }
    var showOpacitySlider by remember { mutableStateOf(false) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(messages.size, lastMessageText) {
        if (messages.isNotEmpty()) {
            val lastIndex = messages.size - 1

            if (lastMessageIsStreaming) {
                listState.scrollToItem(lastIndex)
            } else {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    if (isCollapsed) {
        Box(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onWindowDrag(dragAmount.x, dragAmount.y)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onToggleCollapse,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                if (isRecording) {
                    Icon(
                        Icons.Default.GraphicEq,
                        "Expand",
                        Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.ChatBubble,
                        "Expand",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (isRecording) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onError, CircleShape)
                )
            }
        }
    } else {
        Box {
            Card(
                modifier = Modifier.fillMaxSize()
                    .widthIn(min = 200.dp)
                    .heightIn(min = 100.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onWindowDrag(dragAmount.x, dragAmount.y)
                        }
                    },
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val statusColor = when {
                                isPreparing -> MaterialTheme.colorScheme.tertiary
                                isRecording -> MaterialTheme.colorScheme.error
                                else -> Color.Green
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color = statusColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            val statusText = when {
                                isPreparing -> "Menyiapkan..."
                                isRecording -> "Mendengarkan..."
                                else -> "Rangkum AI"
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.height(42.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            IconButton(
                                onClick = { showOpacitySlider = !showOpacitySlider },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    "Transparansi",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = onToggleCollapse,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Minimize,
                                    "Kecilkan",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 11.8.dp)
                                )
                            }
                            IconButton(
                                onClick = onCloseApp,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Tutup",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (showOpacitySlider) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .height(22.dp)
                        ) {
                            Slider(
                                modifier = Modifier.fillMaxWidth(),
                                value = currentAlpha,
                                onValueChange = {
                                    currentAlpha = it
                                    onOpacityChange(it)
                                },
                                valueRange = 0.3f..1f,
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height(24.dp)
                                            .shadow(4.dp, CircleShape)
                                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) { Text(text = "${(currentAlpha * 100).toInt()}%", fontSize = 8.sp)}
                                },
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        sliderState = sliderState,
                                        modifier = Modifier.height(3.dp)
                                    )
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(
                            messages,
                            key = { message -> message.id }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                onEditSave = { newPrompt ->
                                    onRegenerateResponse(newPrompt)
                                },
                                onRetry = {
                                    val currentIndex = messages.indexOf(message)
                                    if (currentIndex > 0) {
                                        val userPrompt = messages[currentIndex - 1].text
                                        onRegenerateResponse(userPrompt)
                                    }
                                },
                                onShowMenu = { selectedMessage = message }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (isLoading) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        isPreparing -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Menghubungkan Audio...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        isRecording -> {
                            var isEditing by remember { mutableStateOf(false) }
                            var tempEditText by remember { mutableStateOf(TextFieldValue("")) }
                            val bringIntoViewRequester = remember { BringIntoViewRequester() }
                            val scope = rememberCoroutineScope()
                            val scrollState = rememberScrollState()

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                        .heightIn(min = 100.dp, max = 150.dp)
                                        .background(Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .verticalScroll(scrollState)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isEditing) "Mode Edit:" else "Live Transkrip:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            if (!isEditing && activeModelName != null) {
                                                Spacer(modifier = Modifier.width(8.dp))

                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(
                                                        0.5.dp,
                                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = activeModelName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (isEditing) {
                                            BasicTextField(
                                                value = tempEditText,
                                                onValueChange = { tempEditText = it },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .bringIntoViewRequester(bringIntoViewRequester),
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                decorationBox = { innerTextField ->
                                                    if (tempEditText.text.isEmpty()) {
                                                        Text(
                                                            text = "Edit transkrip...",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                    innerTextField()
                                                },
                                                onTextLayout = { layoutResult ->
                                                    val cursorRect = layoutResult.getCursorRect(tempEditText.selection.start)
                                                    scope.launch { bringIntoViewRequester.bringIntoView(cursorRect) }
                                                }
                                            )
                                        } else {
                                            LaunchedEffect(liveTranscript) {
                                                if (!isEditing) {
                                                    scrollState.animateScrollTo(scrollState.maxValue)
                                                }
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                if (liveTranscript.isNotBlank()) {
                                                    Text(
                                                        text = liveTranscript,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontStyle = FontStyle.Italic,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                } else {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    ) {
                                                        LinearProgressIndicator(
                                                            modifier = Modifier.width(60.dp)
                                                                .height(2.dp),
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                                alpha = 0.3f
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            "Mendengarkan...",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                                alpha = 0.7f
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val currentMaxWidth = maxWidth
                                    val showSmallTools = currentMaxWidth > 240.dp

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isEditing) {
                                            SmallFloatingActionButton(
                                                onClick = { isEditing = false },
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Close, "Batal Edit")
                                            }

                                            Button(
                                                onClick = {
                                                    onUpdateTranscript(tempEditText.text)
                                                    isEditing = false
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                modifier = Modifier.weight(1f)
                                                    .height(36.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Check, null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Simpan")
                                            }

                                        } else {
                                            AnimatedVisibility(
                                                visible = showSmallTools,
                                                enter = expandHorizontally() + fadeIn(),
                                                exit = shrinkHorizontally() + fadeOut()
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    SmallFloatingActionButton(
                                                        onClick = {
                                                            tempEditText = TextFieldValue(
                                                                text = liveTranscript,
                                                                selection = TextRange(liveTranscript.length)
                                                            )
                                                            isEditing = true
                                                        },
                                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            "Edit",
                                                            Modifier.size(20.dp)
                                                        )
                                                    }

                                                    SmallFloatingActionButton(
                                                        onClick = onResetTranscript,
                                                        containerColor = MaterialTheme.colorScheme.secondary,
                                                        contentColor = MaterialTheme.colorScheme.onSecondary,
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Refresh,
                                                            "Reset",
                                                            Modifier.size(20.dp)
                                                        )
                                                    }

                                                    SmallFloatingActionButton(
                                                        onClick = onCancelRecording,
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            "Batal",
                                                            Modifier.size(20.dp)
                                                        )
                                                    }

                                                    SmallFloatingActionButton(
                                                        onClick = onTogglePause,
                                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                                        contentColor = MaterialTheme.colorScheme.onTertiary,
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                            contentDescription = if (isPaused) "Lanjutkan" else "Jeda",
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = onStopRecording,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    modifier = Modifier.size(20.dp),
                                                    imageVector = Icons.Default.Stop,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (currentMaxWidth > 100.dp) {
                                                    Text(
                                                        text = "Stop",
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                            ) {
                                IconButton(
                                    onClick = onStartRecording,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.GraphicEq, "Rekam Lagi")
                                }

                                var isFocused by remember { mutableStateOf(false) }

                                BasicTextField(
                                    value = userInput,
                                    onValueChange = { onInputChange(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(43.dp)
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
                                    singleLine = true,
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ) {
                                            if (userInput.isEmpty()) {
                                                Text(
                                                    text = "Tanya detail...",
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

                                IconButton(
                                    onClick = onSendMessage,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        modifier = Modifier.offset(x = 2.dp)
                                            .size(22.dp),
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Kirim"
                                    )
                                }
                            }
                        }
                    }
                }
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Opsi Pesan",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ActionIcon(
                                    icon = Icons.Default.ContentCopy,
                                    label = "Salin semua",
                                    onClick = {
                                        scope.launch {
                                            val clipData = ClipData.newPlainText("Rangkum AI", message.text)
                                            clipboard.setClipEntry(ClipEntry(clipData))
                                        }
                                        selectedMessage = null
                                    }
                                )

                                if (message.isUser) {
                                    ActionIcon(
                                        icon = Icons.Default.Edit,
                                        label = "Edit",
                                        onClick = {
                                            message.isEditing = true
                                            selectedMessage = null
                                        }
                                    )
                                }

                                ActionIcon(
                                    icon = Icons.Default.Close,
                                    label = "Tutup",
                                    onClick = { selectedMessage = null }
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.NorthWest,
                contentDescription = "Resize",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .rotate(180f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (accumulatedDragX != 0f || accumulatedDragY != 0f) {
                                    onWindowResize(accumulatedDragX, accumulatedDragY)
                                    accumulatedDragX = 0f
                                    accumulatedDragY = 0f
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            accumulatedDragX -= dragAmount.x
                            accumulatedDragY -= dragAmount.y

                            if (abs(accumulatedDragX) >= 50f || abs(accumulatedDragY) >= 50f) {
                                onWindowResize(accumulatedDragX, accumulatedDragY)
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                            }
                        }
                    }
            )
        }
    }
}