package com.achmadichzan.rangkum.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.presentation.utils.MarkdownBlock
import com.achmadichzan.rangkum.presentation.utils.MarkdownParser
import com.achmadichzan.rangkum.presentation.utils.RenderMarkdownBlock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun MessageBubble(
    message: UiMessage,
    onRetry: (String) -> Unit,
    onEditSave: (String) -> Unit,
    onShowMenu: () -> Unit
) {
    val bubbleColor = if (message.isError) MaterialTheme.colorScheme.errorContainer
    else if (message.isUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (message.isError) MaterialTheme.colorScheme.onErrorContainer
    else if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    val alignment = if (message.isUser) Alignment.End else Alignment.Start

    var editedText by remember { mutableStateOf(TextFieldValue(message.text)) }
    val focusRequester = remember { FocusRequester() }

    var displayedText by remember(message.id) {
        mutableStateOf(if (message.isStreaming) "" else message.text)
    }
    val wordQueue = remember { Channel<String>(Channel.UNLIMITED) }
    var lastProcessedIndex by remember(message.id) {
        mutableIntStateOf(if (message.isStreaming) 0 else message.text.length)
    }

    LaunchedEffect(message.text) {
        if (message.isStreaming) {
            val fullText = message.text

            if (fullText.length < lastProcessedIndex) {
                displayedText = ""
                lastProcessedIndex = 0
            }

            if (fullText.length > lastProcessedIndex) {
                val newChunk = fullText.substring(lastProcessedIndex)
                val words = newChunk.split(Regex("(?<=\\s)"))

                words.forEach { word ->
                    wordQueue.send(word)
                }
                lastProcessedIndex = fullText.length
            }
        } else {
            if (displayedText != message.text) {
                displayedText = message.text
                lastProcessedIndex = message.text.length
            }
        }
    }

    LaunchedEffect(message.id) {
        wordQueue.receiveAsFlow().collect { word ->
            displayedText += word
            delay(20)
        }
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (displayedText.isNotEmpty()) 1f else 0f,
        animationSpec = tween(500)
    )

    Column(
        modifier = Modifier.fillMaxWidth()
            .graphicsLayer { alpha = alphaAnim },
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .combinedClickable(
                    onClick = { /* apa hayo */ },
                    onLongClick = { if (!message.isEditing) onShowMenu() }
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.isEditing) {
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }

                    BasicTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (editedText.text.isEmpty()) {
                                Text(
                                    text = "Masukkan prompt...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { message.isEditing = false }) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                message.text = editedText.text
                                message.isEditing = false

                                onEditSave(editedText.text)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Kirim")
                        }
                    }
                }
                else {
                    val useFastRendering = message.isStreaming

                    if (useFastRendering) {
                        val fastStyledText = remember(displayedText) {
                            MarkdownParser.parse(
                                text = displayedText,
                                primaryColor = textColor
                            )
                        }

                        SelectionContainer {
                            Text(
                                text = fastStyledText,
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        val blocks = remember(displayedText) {
                            MarkdownParser.parseToBlocks(
                                text = displayedText,
                                primaryColor = textColor,
                                onBackground = textColor
                            )
                        }

                        SelectionContainer {
                            Column(modifier = Modifier.animateContentSize()) {
                                blocks.forEachIndexed { index, block ->
                                    key(index) {
                                        RenderMarkdownBlock(block, textColor)
                                    }
                                    val isCurrentList = block is MarkdownBlock.ListBullet
                                            || block is MarkdownBlock.ListNumber
                                    val nextBlock = blocks.getOrNull(index + 1)
                                    val isNextList = nextBlock is MarkdownBlock.ListBullet
                                            || nextBlock is MarkdownBlock.ListNumber

                                    if (isCurrentList && !isNextList) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isError && !message.isUser) {
                            IconButton(
                                onClick = { onRetry(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    "Coba Lagi",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Gagal. Coba lagi?",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (message.isUser) {
                            IconButton(
                                onClick = {
                                    val currentText = message.text
                                    editedText = TextFieldValue(
                                        text = message.text,
                                        selection = TextRange(currentText.length)
                                    )
                                    message.isEditing = true
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    "Edit Prompt",
                                    tint = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}