package com.achmadichzan.rangkum.data.repository

import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class AiRepositoryImpl(
    private val generativeModel: GenerativeModel
) : AiRepository {

    override suspend fun sendMessage(prompt: String, history: List<Message>): String {
        val geminiHistory = history.map { it.toGeminiContent() }

        val chat = generativeModel.startChat(history = geminiHistory)

        val response = chat.sendMessage(prompt)

        return response.text ?: throw Exception("Respon AI kosong")
    }

    override fun generateContentStream(prompt: String): Flow<String> {
        return generativeModel.generateContentStream(prompt)
            .map { chunk ->
                chunk.text ?: ""
            }
    }

    private fun Message.toGeminiContent(): Content {
        val roleStr = if (this.isUser) "user" else "model"
        return content(role = roleStr) {
            text(this@toGeminiContent.text)
        }
    }
}