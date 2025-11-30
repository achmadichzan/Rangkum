package com.achmadichzan.rangkum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.usecase.DeleteSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.RenameSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    getHistoryUseCase: GetHistoryUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val renameSessionUseCase: RenameSessionUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val allSessions = getHistoryUseCase.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val isDarkMode = settingsRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            deleteSessionUseCase(sessionId)
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            renameSessionUseCase(sessionId, newTitle)
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleTheme(!isDark)
        }
    }
}