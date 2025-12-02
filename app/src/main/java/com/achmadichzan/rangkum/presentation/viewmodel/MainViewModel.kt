package com.achmadichzan.rangkum.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.Session
import com.achmadichzan.rangkum.domain.repository.ChatRepository
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.usecase.DeleteSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.GetYoutubeTranscriptUseCase
import com.achmadichzan.rangkum.domain.usecase.RenameSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.RestoreSessionUseCase
import com.achmadichzan.rangkum.presentation.utils.PromptUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val renameSessionUseCase: RenameSessionUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val settingsRepository: SettingsRepository,
    private val getYoutubeTranscriptUseCase: GetYoutubeTranscriptUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val allSessions = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            getHistoryUseCase.getSessions(query)
        }
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
    var isYoutubeLoading by mutableStateOf(false)
        private set
    var youtubeError by mutableStateOf<String?>(null)
        private set
    private var deletedSession: Session? = null
    private var deletedMessages: List<Message>? = null
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            renameSessionUseCase(sessionId, newTitle)
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleTheme(!isDark)
        }
    }

    fun processYoutubeLink(url: String, onSuccess: (Long) -> Unit) {
        isYoutubeLoading = true
        youtubeError = null

        viewModelScope.launch {
            try {
                val response = getYoutubeTranscriptUseCase(url)
                val rawTranscript = response.transcript ?: ""
                val videoTitle = response.title ?: "Rangkuman YouTube"
                val fullPrompt = PromptUtils.create(rawTranscript)
                val sessionId = chatRepository.createSession(videoTitle)

                chatRepository.saveMessage(sessionId, fullPrompt, isUser = true)

                onSuccess(sessionId)
            } catch (e: Exception) {
                youtubeError = e.message ?: "Terjadi kesalahan"
            } finally {
                isYoutubeLoading = false
            }
        }
    }

    fun clearError() { youtubeError = null }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            val session = getHistoryUseCase.getSession(sessionId)
            val messages = getHistoryUseCase.getMessages(sessionId)

            if (session != null) {
                deletedSession = session
                deletedMessages = messages

                deleteSessionUseCase(sessionId)

                _uiEvent.send(
                    UiEvent.ShowSnackbar("Menghapus '${session.title}'")
                )
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            if (deletedSession != null && deletedMessages != null) {
                restoreSessionUseCase(deletedSession!!, deletedMessages!!)

                deletedSession = null
                deletedMessages = null
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}