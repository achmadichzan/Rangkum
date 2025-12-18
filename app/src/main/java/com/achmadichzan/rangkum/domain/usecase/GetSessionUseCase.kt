package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository

class GetSessionUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: Long): Session? {
        return chatRepository.getSessionById(sessionId)
    }
}