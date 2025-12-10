package com.achmadichzan.rangkum.data.repository

import com.achmadichzan.rangkum.data.preferences.UserPreferences
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val userPreferences: UserPreferences
) : SettingsRepository {
    override val isDarkMode: Flow<Boolean?> = userPreferences.isDarkMode

    override suspend fun toggleTheme(isDark: Boolean) {
        userPreferences.toggleTheme(isDark)
    }

    override val selectedModel: Flow<String> = userPreferences.selectedModel

    override suspend fun setModel(modelName: String) {
        userPreferences.setModel(modelName)
    }
}