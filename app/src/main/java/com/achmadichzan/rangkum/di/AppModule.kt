package com.achmadichzan.rangkum.di

import com.achmadichzan.rangkum.data.local.AppDatabase
import com.achmadichzan.rangkum.data.preferences.UserPreferences
import com.achmadichzan.rangkum.data.repository.AiRepositoryImpl
import com.achmadichzan.rangkum.data.repository.ChatRepositoryImpl
import com.achmadichzan.rangkum.data.repository.SettingsRepositoryImpl
import com.achmadichzan.rangkum.data.repository.YoutubeRepositoryImpl
import com.achmadichzan.rangkum.domain.repository.AiRepository
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.repository.YoutubeRepository
import com.achmadichzan.rangkum.domain.usecase.CreateSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSettingsUseCase
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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.gson.gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient(Android) {
            install(ContentNegotiation) { gson() }
            install(Logging) { level = LogLevel.BODY }
        }
    }
}
val databaseModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().chatDao() }
    single { UserPreferences(androidContext()) }
}
val repositoryModule = module {
    singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }
    singleOf(::AiRepositoryImpl) { bind<AiRepository>() }
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
    singleOf(::YoutubeRepositoryImpl) { bind<YoutubeRepository>() }
}
val domainModule = module {
    singleOf(::SummarizeTranscriptUseCase)
    singleOf(::GetHistoryUseCase)
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
}
val viewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::MainViewModel)
}