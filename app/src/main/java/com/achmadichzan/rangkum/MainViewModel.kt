package com.achmadichzan.rangkum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.data.local.AppDatabase
import com.achmadichzan.rangkum.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).chatDao()
    private val userPreferences = UserPreferences(application)

    val allSessions = dao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val isDarkMode = userPreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun toggleTheme(currentValue: Boolean?) { // Terima Boolean?
        viewModelScope.launch {
            val nextValue = !(currentValue ?: false)
            userPreferences.toggleTheme(nextValue)
        }
    }
}