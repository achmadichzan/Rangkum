package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository

class RestoreSessionUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(session: Session, messages: List<Message>) {
        val newSessionId = chatRepository.createSessionWithId(session)

        messages.forEach { msg ->
            chatRepository.saveMessage(
                sessionId = newSessionId,
                text = msg.text,
                isUser = msg.isUser,
            )
        }
    }
}