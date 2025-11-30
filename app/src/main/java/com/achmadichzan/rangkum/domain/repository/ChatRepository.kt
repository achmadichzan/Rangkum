package com.achmadichzan.rangkum.domain.repository

import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun createSession(title: String): Long
    suspend fun getMessagesBySessionId(sessionId: Long): List<Message>
    suspend fun saveMessage(sessionId: Long, text: String, isUser: Boolean)
    suspend fun deleteSession(sessionId: Long)
    suspend fun renameSession(sessionId: Long, newTitle: String)
    fun searchSessions(query: String): Flow<List<Session>>
}