package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.ChatRepository

class DeleteSessionUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: Long) {
        chatRepository.deleteSession(sessionId)
    }
}