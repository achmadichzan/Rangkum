package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.ChatRepository

class SaveMessageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: Long, text: String, isUser: Boolean): Long {
        return chatRepository.saveMessage(sessionId, text, isUser)
    }
}