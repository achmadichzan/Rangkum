package com.achmadichzan.rangkum.di

import com.achmadichzan.rangkum.data.local.AppDatabase
import com.achmadichzan.rangkum.data.preferences.UserPreferences
import com.achmadichzan.rangkum.data.repository.AiRepositoryImpl
import com.achmadichzan.rangkum.data.repository.ChatRepositoryImpl
import com.achmadichzan.rangkum.data.repository.ModelRepositoryImpl
import com.achmadichzan.rangkum.data.repository.SettingsRepositoryImpl
import com.achmadichzan.rangkum.data.repository.YoutubeRepositoryImpl
import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import com.achmadichzan.rangkum.domain.repository.ModelRepository
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.repository.YoutubeRepository
import com.achmadichzan.rangkum.domain.usecase.CreateSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteVoskModelUseCase
import com.achmadichzan.rangkum.domain.usecase.DownloadVoskModelUseCase
import com.achmadichzan.rangkum.domain.usecase.GetActiveVoskModelPathUseCase
import com.achmadichzan.rangkum.domain.usecase.GetMessagesUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSessionsUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSettingsUseCase
import com.achmadichzan.rangkum.domain.usecase.GetVoskModelStatusUseCase
import com.achmadichzan.rangkum.domain.usecase.GetYoutubeTranscriptUseCase
import com.achmadichzan.rangkum.domain.usecase.RenameSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.RestoreSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.SaveMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.SummarizeTranscriptUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateSettingsUseCase
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import com.achmadichzan.rangkum.presentation.viewmodels.MainViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single(named("apiClient")) {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            install(Logging) { level = LogLevel.BODY }
        }
    }
    single(named("downloadClient")) {
        HttpClient(Android) {
            install(Logging) { level = LogLevel.HEADERS }
            install(HttpTimeout) {
                requestTimeoutMillis = null
                socketTimeoutMillis = null
                connectTimeoutMillis = null
            }
        }
    }
}
val databaseModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().chatDao() }
    single { UserPreferences(androidContext()) }
}
val dispatcherModule = module {
    single(named("io")) { Dispatchers.IO }
}
val repositoryModule = module {
    single<ChatRepository> {
        ChatRepositoryImpl(
            chatDao = get(),
            ioDispatcher = get(named("io"))
        )
    }
    single<AiRepository> {
        AiRepositoryImpl(
            ioDispatcher = get(named("io"))
        )
    }
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
    single<YoutubeRepository> {
        YoutubeRepositoryImpl(
            client = get(named("apiClient")),
            ioDispatcher = get(named("io"))
        )
    }
    single<ModelRepository> {
        ModelRepositoryImpl(
            context = androidContext(),
            client = get(named("downloadClient")),
            ioDispatcher = get(named("io"))
        )
    }
}
val domainModule = module {
    singleOf(::SummarizeTranscriptUseCase)
    singleOf(::GetSessionsUseCase)
    singleOf(::GetSessionUseCase)
    singleOf(::GetMessagesUseCase)
    singleOf(::UpdateMessageUseCase)
    singleOf(::DeleteSessionUseCase)
    singleOf(::RenameSessionUseCase)
    singleOf(::UpdateSessionUseCase)
    singleOf(::DeleteMessageUseCase)
    singleOf(::SaveMessageUseCase)
    singleOf(::GetSettingsUseCase)
    singleOf(::UpdateSettingsUseCase)
    singleOf(::CreateSessionUseCase)
    singleOf(::GetYoutubeTranscriptUseCase)
    singleOf(::RestoreSessionUseCase)
    singleOf(::GetActiveVoskModelPathUseCase)
    singleOf(::DownloadVoskModelUseCase)
    singleOf(::GetVoskModelStatusUseCase)
    singleOf(::DeleteVoskModelUseCase)
}
val viewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::MainViewModel)
}