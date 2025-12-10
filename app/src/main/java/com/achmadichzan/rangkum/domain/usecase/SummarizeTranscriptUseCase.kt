package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

class SummarizeTranscriptUseCase(
    private val aiRepository: AiRepository,
    private val chatRepository: ChatRepository
) {
    operator fun invoke(sessionId: Long, prompt: String, modelName: String): Flow<String> {
        val stringBuilder = StringBuilder()

        return flow {
            val history = chatRepository.getMessagesBySessionId(sessionId)
            val aiFlow = aiRepository.generateContentStream(prompt, history, modelName)

            aiFlow.collect { chunk ->
                stringBuilder.append(chunk)
                emit(chunk)
            }
        }
        .onCompletion {
            val fullSummary = stringBuilder.toString()
            if (fullSummary.isNotBlank()) {
                chatRepository.saveMessage(sessionId, fullSummary, isUser = false)
            }
        }
    }
}