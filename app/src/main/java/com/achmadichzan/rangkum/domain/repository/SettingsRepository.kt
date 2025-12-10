package com.achmadichzan.rangkum.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isDarkMode: Flow<Boolean?>
    suspend fun toggleTheme(isDark: Boolean)

    val selectedModel: Flow<String>
    suspend fun setModel(modelName: String)
}