package com.achmadichzan.rangkum.domain.repository

import com.achmadichzan.rangkum.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    fun generateContentStream(
        prompt: String,
        history: List<Message>,
        modelName: String
    ): Flow<String>
}