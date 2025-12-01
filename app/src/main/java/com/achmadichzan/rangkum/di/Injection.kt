package com.achmadichzan.rangkum.di

import android.content.Context
import com.achmadichzan.rangkum.data.local.AppDatabase
import com.achmadichzan.rangkum.data.preferences.UserPreferences
import com.achmadichzan.rangkum.data.remote.GeminiFactory
import com.achmadichzan.rangkum.data.repository.AiRepositoryImpl
import com.achmadichzan.rangkum.data.repository.ChatRepositoryImpl
import com.achmadichzan.rangkum.data.repository.SettingsRepositoryImpl
import com.achmadichzan.rangkum.data.repository.YoutubeRepositoryImpl
import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.repository.YoutubeRepository
import com.achmadichzan.rangkum.domain.usecase.DeleteSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.GetYoutubeTranscriptUseCase
import com.achmadichzan.rangkum.domain.usecase.RenameSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.SendMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.SummarizeTranscriptUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.gson.gson

object Injection {
    private val ktorClient: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                gson() // Menggunakan Gson converter
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

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

    fun provideYoutubeRepository(): YoutubeRepository {
        return YoutubeRepositoryImpl(ktorClient)
    }

    fun provideGetYoutubeTranscriptUseCase(): GetYoutubeTranscriptUseCase {
        return GetYoutubeTranscriptUseCase(provideYoutubeRepository())
    }
}