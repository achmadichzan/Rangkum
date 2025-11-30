package com.achmadichzan.rangkum.domain.repository

import com.achmadichzan.rangkum.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun sendMessage(
        prompt: String,
        history: List<Message>
    ): String

    fun generateContentStream(prompt: String): Flow<String>
}