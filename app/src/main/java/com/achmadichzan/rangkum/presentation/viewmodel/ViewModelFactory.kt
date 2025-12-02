package com.achmadichzan.rangkum.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.achmadichzan.rangkum.di.Injection

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                sendMessageUseCase = Injection.provideSendMessageUseCase(context),
                summarizeUseCase = Injection.provideSummarizeUseCase(context),
                getHistoryUseCase = Injection.provideGetHistoryUseCase(context),
                settingsRepository = Injection.provideSettingsRepository(context)
            ) as T
        }

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                getHistoryUseCase = Injection.provideGetHistoryUseCase(context),
                deleteSessionUseCase = Injection.provideDeleteSessionUseCase(context),
                restoreSessionUseCase = Injection.provideRestoreSessionUseCase(context),
                renameSessionUseCase = Injection.provideRenameSessionUseCase(context),
                settingsRepository = Injection.provideSettingsRepository(context),
                getYoutubeTranscriptUseCase = Injection.provideGetYoutubeTranscriptUseCase(),
                chatRepository = Injection.provideChatRepository(context)
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}