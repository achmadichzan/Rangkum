package com.achmadichzan.rangkum.di

import android.content.Context
import com.achmadichzan.rangkum.data.local.AppDatabase
import com.achmadichzan.rangkum.data.preferences.UserPreferences
import com.achmadichzan.rangkum.data.remote.GeminiFactory
import com.achmadichzan.rangkum.data.repository.AiRepositoryImpl
import com.achmadichzan.rangkum.data.repository.ChatRepositoryImpl
import com.achmadichzan.rangkum.data.repository.SettingsRepositoryImpl
import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.usecase.DeleteSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.RenameSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.SendMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.SummarizeTranscriptUseCase

object Injection {

    fun provideChatRepository(context: Context): ChatRepository {
        val database = AppDatabase.getDatabase(context)
        return ChatRepositoryImpl(database.chatDao())
    }

    fun provideAiRepository(): AiRepository {
        return AiRepositoryImpl(GeminiFactory.createModel())
    }

    fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(UserPreferences(context))
    }

    fun provideSendMessageUseCase(context: Context): SendMessageUseCase {
        return SendMessageUseCase(
            chatRepository = provideChatRepository(context),
            aiRepository = provideAiRepository()
        )
    }

    fun provideSummarizeUseCase(context: Context): SummarizeTranscriptUseCase {
        return SummarizeTranscriptUseCase(
            aiRepository = provideAiRepository(),
            chatRepository = provideChatRepository(context)
        )
    }

    fun provideGetHistoryUseCase(context: Context): GetHistoryUseCase {
        return GetHistoryUseCase(
            chatRepository = provideChatRepository(context)
        )
    }

    fun provideDeleteSessionUseCase(context: Context): DeleteSessionUseCase {
        return DeleteSessionUseCase(provideChatRepository(context))
    }

    fun provideRenameSessionUseCase(context: Context): RenameSessionUseCase {
        return RenameSessionUseCase(provideChatRepository(context))
    }
}