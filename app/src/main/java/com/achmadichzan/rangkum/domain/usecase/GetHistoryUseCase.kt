package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetHistoryUseCase(
    private val chatRepository: ChatRepository
) {
    fun getSessions(query: String = ""): Flow<List<Session>> {
        return if (query.isBlank()) {
            chatRepository.getAllSessions()
        } else {
            chatRepository.searchSessions(query)
        }
    }

    suspend fun getMessages(sessionId: Long): List<Message> {
        return chatRepository.getMessagesBySessionId(sessionId)
    }

    suspend fun createNewSession(title: String): Long {
        return chatRepository.createSession(title)
    }
}