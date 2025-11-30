package com.achmadichzan.rangkum.domain.model

data class Message(
    val id: Long = 0,
    val sessionId: Long,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)