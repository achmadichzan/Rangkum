package com.achmadichzan.rangkum.data.repository

import com.achmadichzan.rangkum.data.remote.GeminiFactory
import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AiRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher
) : AiRepository {
    override fun generateContentStream(
        prompt: String,
        history: List<Message>,
        modelName: String
    ): Flow<String> {
        val generativeModel = GeminiFactory.createModel(modelName)
        val geminiHistory = history
            .filter { it.text.isNotBlank() && !it.isError }
            .map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }

        return flow {
            val chat = generativeModel.startChat(
                history = geminiHistory
            )
            val response = chat.sendMessageStream(prompt)

            response.collect {
                emit(it.text ?: "")
            }
        }
            .flowOn(ioDispatcher)
    }
}