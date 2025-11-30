package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository,
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(sessionId: Long, text: String): String {
        chatRepository.saveMessage(sessionId, text, isUser = true)

        val history = chatRepository.getMessagesBySessionId(sessionId)
        val aiResponse = aiRepository.sendMessage(text, history)

        chatRepository.saveMessage(sessionId, aiResponse, isUser = false)

        return aiResponse
    }
}