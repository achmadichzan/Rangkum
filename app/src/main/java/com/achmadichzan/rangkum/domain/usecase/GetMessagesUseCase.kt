package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.repository.ChatRepository

class GetMessagesUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: Long): List<Message> {
        return chatRepository.getMessagesBySessionId(sessionId)
    }
}