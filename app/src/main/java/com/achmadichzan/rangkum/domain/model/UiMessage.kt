package com.achmadichzan.rangkum.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

data class UiMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val isError: Boolean = false,
    private val initialText: String = "",
    private val initialIsStreaming: Boolean = false

) {
    var text by mutableStateOf(initialText)
    var isStreaming by mutableStateOf(initialIsStreaming)
    var isEditing by mutableStateOf(false)
}