package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.SettingsRepository

class UpdateSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend fun setDarkMode(isDark: Boolean) {
        settingsRepository.toggleTheme(isDark)
    }

    suspend fun setModel(modelName: String) {
        settingsRepository.setModel(modelName)
    }
}