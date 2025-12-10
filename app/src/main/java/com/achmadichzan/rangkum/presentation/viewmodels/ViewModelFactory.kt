package com.achmadichzan.rangkum.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.achmadichzan.rangkum.di.Injection

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                summarizeUseCase = Injection.provideSummarizeUseCase(context),
                getHistoryUseCase = Injection.provideGetHistoryUseCase(context),
                updateMessageUseCase = Injection.provideUpdateMessageUseCase(context),
                deleteMessageUseCase = Injection.provideDeleteMessageUseCase(context),
                saveMessageUseCase = Injection.provideSaveMessageUseCase(context),
                getSettingsUseCase = Injection.provideGetSettingsUseCase(context),
                updateSettingsUseCase = Injection.provideUpdateSettingsUseCase(context)
            ) as T
        }

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                getHistoryUseCase = Injection.provideGetHistoryUseCase(context),
                deleteSessionUseCase = Injection.provideDeleteSessionUseCase(context),
                restoreSessionUseCase = Injection.provideRestoreSessionUseCase(context),
                renameSessionUseCase = Injection.provideRenameSessionUseCase(context),
                updateSessionUseCase = Injection.provideUpdateSessionUseCase(context),
                getYoutubeTranscriptUseCase = Injection.provideGetYoutubeTranscriptUseCase(),
                getSettingsUseCase = Injection.provideGetSettingsUseCase(context),
                saveMessageUseCase = Injection.provideSaveMessageUseCase(context),
                updateSettingsUseCase = Injection.provideUpdateSettingsUseCase(context),
                createSessionUseCase = Injection.provideCreateSessionUseCase(context)
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}