package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

class SummarizeTranscriptUseCase(
    private val aiRepository: AiRepository,
    private val chatRepository: ChatRepository
) {
    operator fun invoke(sessionId: Long, fullPrompt: String): Flow<String> {

        val stringBuilder = StringBuilder()

        return aiRepository.generateContentStream(fullPrompt)
            .onCompletion {
                val fullSummary = stringBuilder.toString()
                if (fullSummary.isNotBlank()) {
                    chatRepository.saveMessage(sessionId, fullSummary, isUser = false)
                }
            }
            .map { chunk ->
                stringBuilder.append(chunk)
                chunk
            }
    }
}