package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.ChatRepository

class UpdateMessageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(messageId: Long, newText: String) {
        chatRepository.updateMessage(messageId, newText)
    }
}