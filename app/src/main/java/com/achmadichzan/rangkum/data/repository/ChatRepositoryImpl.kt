package com.achmadichzan.rangkum.data.repository

import com.achmadichzan.rangkum.data.local.ChatDao
import com.achmadichzan.rangkum.data.local.ChatMessageEntity
import com.achmadichzan.rangkum.data.local.ChatSessionEntity
import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(private val chatDao: ChatDao) : ChatRepository {
    override fun getAllSessions(): Flow<List<Session>> {
        return chatDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSessionById(sessionId: Long): Session? {
        return chatDao.getSessionById(sessionId)?.toDomain()
    }

    override suspend fun createSession(title: String): Long {
        return chatDao.insertSession(
            ChatSessionEntity(title = title)
        )
    }

    override suspend fun createSessionWithId(session: Session): Long {
        val entity = ChatSessionEntity(
            id = session.id,
            title = session.title,
            timestamp = session.timestamp
        )
        return chatDao.insertSession(entity)
    }

    override suspend fun getMessagesBySessionId(sessionId: Long): List<Message> {
        return chatDao.getMessagesBySessionId(sessionId).map { it.toDomain() }
    }

    override suspend fun saveMessage(sessionId: Long, text: String, isUser: Boolean): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                text = text,
                isUser = isUser
            )
        )
    }

    override suspend fun updateMessage(messageId: Long, newText: String) {
        chatDao.updateMessageText(messageId, newText)
    }

    override suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSessionById(sessionId)
    }

    override suspend fun renameSession(sessionId: Long, newTitle: String) {
        chatDao.updateSessionTitle(sessionId, newTitle)
    }

    override fun searchSessions(query: String): Flow<List<Session>> {
        return chatDao.searchSessions(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun ChatSessionEntity.toDomain(): Session {
        return Session(
            id = this.id,
            title = this.title,
            timestamp = this.timestamp
        )
    }

    private fun ChatMessageEntity.toDomain(): Message {
        return Message(
            id = this.id,
            sessionId = this.sessionId,
            text = this.text,
            isUser = this.isUser,
            timestamp = this.timestamp
        )
    }
}