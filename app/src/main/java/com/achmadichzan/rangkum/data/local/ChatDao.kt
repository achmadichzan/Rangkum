package com.achmadichzan.rangkum.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): kotlinx.coroutines.flow.Flow<List<ChatSession>>
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySessionId(sessionId: Long): List<ChatMessageEntity>
}