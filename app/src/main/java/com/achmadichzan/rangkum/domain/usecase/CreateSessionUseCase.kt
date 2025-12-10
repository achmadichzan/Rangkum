package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.ChatRepository

class CreateSessionUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(title: String): Long {
        return repository.createSession(title)
    }
}