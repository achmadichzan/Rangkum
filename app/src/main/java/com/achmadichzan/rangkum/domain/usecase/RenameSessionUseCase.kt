package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.ChatRepository

class RenameSessionUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(sessionId: Long, newTitle: String) {
        chatRepository.renameSession(sessionId, newTitle)
    }
}