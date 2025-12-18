package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetSessionsUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(query: String = ""): Flow<List<Session>> {
        return if (query.isBlank()) chatRepository.getAllSessions()
        else chatRepository.searchSessions(query)
    }
}