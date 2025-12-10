package com.achmadichzan.rangkum.presentation.screen

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.achmadichzan.rangkum.domain.model.UiMessage
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
    val listState = rememberLazyListState()
    val lastMessage = viewModel.messages.lastOrNull()
    val lastMessageText = lastMessage?.text ?: ""
    val lastMessageIsStreaming = lastMessage?.isStreaming ?: false
    var selectedMessage by remember { mutableStateOf<UiMessage?>(null) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.messages.size, lastMessageText) {
        if (viewModel.messages.isNotEmpty()) {
            val lastIndex = viewModel.messages.size - 1

            if (lastMessageIsStreaming) {
                listState.scrollToItem(lastIndex)
            } else {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    var currentAlpha by remember { mutableFloatStateOf(1f) }
    var showOpacitySlider by remember { mutableStateOf(false) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    if (isCollapsed) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
        Box(modifier = Modifier.padding(12.dp)) {
            Card(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onWindowDrag(dragAmount.x, dragAmount.y)
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            IconButton(onClick = { showOpacitySlider = !showOpacitySlider }) {
                                Icon(
                                    Icons.Default.Visibility,
                                    "Transparansi",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onToggleCollapse) {
                                Icon(
                                    Icons.Default.Minimize,
                                    "Kecilkan",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 15.dp)
                                )
                            }
                            IconButton(onClick = onCloseApp) {
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "Transparansi: ${(currentAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = currentAlpha,
                                    onValueChange = {
                                        currentAlpha = it
                                        onOpacityChange(it)
                                    },
                                    valueRange = 0.3f..1f
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(
                            viewModel.messages,
                            key = { message -> message.id }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                onEditSave = { newPrompt ->
                                    viewModel.regenerateResponse(newPrompt)
                                },
                                onRetry = {
                                    val currentIndex = viewModel.messages.indexOf(message)
                                    if (currentIndex > 0) {
                                        val userPrompt = viewModel.messages[currentIndex - 1].text
                                        viewModel.regenerateResponse(userPrompt)
                                    }
                                },
                                onShowMenu = { selectedMessage = message }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (viewModel.isLoading) {
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
                                        .heightIn(min = 100.dp, max = 200.dp)
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
                                            LaunchedEffect(viewModel.liveTranscript) {
                                                if (!isEditing) {
                                                    scrollState.animateScrollTo(scrollState.maxValue)
                                                }
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                if (viewModel.liveTranscript.isNotBlank()) {
                                                    Text(
                                                        text = viewModel.liveTranscript,
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
                                                            )
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
                                    val showSmallTools = currentMaxWidth > 280.dp

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
                                                modifier = Modifier.size(48.dp)
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
                                                    .height(48.dp),
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
                                                            val currentText = viewModel.liveTranscript
                                                            tempEditText = TextFieldValue(
                                                                text = currentText,
                                                                selection = TextRange(currentText.length)
                                                            )
                                                            isEditing = true
                                                        },
                                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.size(48.dp)
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
                                                        modifier = Modifier.size(48.dp)
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
                                                        modifier = Modifier.size(48.dp)
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
                                                        modifier = Modifier.size(48.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                            contentDescription = if (isPaused) "Lanjutkan" else "Jeda",
                                                            modifier = Modifier.size(24.dp)
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
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Stop, null)
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
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
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

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = viewModel.userInput,
                                    onValueChange = { viewModel.onInputChange(it) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Tanya detail...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { viewModel.sendMessage() },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, "Kirim")
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
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag Handle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(bottom = 8.dp)
                    .size(32.dp)
            )

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

                            if (abs(accumulatedDragX) >= 15f || abs(accumulatedDragY) >= 15f) {
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