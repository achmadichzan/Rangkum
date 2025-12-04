package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository

class UpdateSessionUseCase (private val chatRepository: ChatRepository) {
    suspend operator fun invoke(session: Session) {
        chatRepository.updateSession(session)
    }
}