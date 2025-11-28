package com.achmadichzan.rangkum.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    val isDarkMode: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE]
        }

    suspend fun toggleTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }
}
