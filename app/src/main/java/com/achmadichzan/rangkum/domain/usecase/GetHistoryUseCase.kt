package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetHistoryUseCase(
    private val chatRepository: ChatRepository
) {
    fun getAllSessions(): Flow<List<Session>> {
        return chatRepository.getAllSessions()
    }

    suspend fun getMessages(sessionId: Long): List<Message> {
        return chatRepository.getMessagesBySessionId(sessionId)
    }

    suspend fun createNewSession(title: String): Long {
        return chatRepository.createSession(title)
    }
}