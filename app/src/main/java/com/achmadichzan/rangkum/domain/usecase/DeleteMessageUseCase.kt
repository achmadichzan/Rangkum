package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.ChatRepository

class DeleteMessageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(messageId: Long) {
        chatRepository.deleteMessage(messageId)
    }
}