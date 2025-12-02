package com.achmadichzan.rangkum.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.SendMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.SummarizeTranscriptUseCase
import com.achmadichzan.rangkum.presentation.utils.PromptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val summarizeUseCase: SummarizeTranscriptUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val messages = mutableStateListOf<UiMessage>()
    var userInput by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var liveTranscript by mutableStateOf("")
        private set
    val isDarkMode = settingsRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    private var currentSessionId: Long? = null

    fun onInputChange(newValue: String) {
        userInput = newValue
    }

    fun updateLiveTranscript(text: String) {
        liveTranscript = text
    }

    fun clearLiveTranscript() {
        liveTranscript = ""
    }

    fun startNewSession() {
        viewModelScope.launch {
            currentSessionId = getHistoryUseCase.createNewSession("Percakapan Baru ${System.currentTimeMillis()}")

            withContext(Dispatchers.Main) {
                messages.clear()
                liveTranscript = ""
            }
        }
    }

    fun loadHistorySession(sessionId: Long) {
        isLoading = true
        currentSessionId = sessionId

        viewModelScope.launch {
            val historyDomain = getHistoryUseCase.getMessages(sessionId)

            withContext(Dispatchers.Main) {
                messages.clear()
                messages.addAll(historyDomain.map { it.toUiModel() })
                isLoading = false
            }
        }
    }

    fun sendMessage() {
        val textToSend = userInput.trim()
        if (textToSend.isEmpty()) return

        messages.add(UiMessage(initialText = textToSend, isUser = true))
        userInput = ""
        isLoading = true

        viewModelScope.launch {
            try {
                if (currentSessionId == null) startNewSession()
                val sessionId = currentSessionId!!

                val aiResponseText = sendMessageUseCase(sessionId, textToSend)

                messages.add(UiMessage(initialText = aiResponseText, isUser = false))

            } catch (e: Exception) {
                messages.add(
                    UiMessage(
                        initialText = "Error: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun sendTextToGemini(transcript: String) {
        val fullPrompt = PromptUtils.create(transcript)

        sendMessageCore(fullPrompt)
    }

    fun regenerateResponse(newPrompt: String) {
        val lastUserIndex = messages.indexOfLast { it.isUser }
        if (lastUserIndex != -1) {
            messages[lastUserIndex].text = newPrompt
        }
        val hasAiBubble = messages.any { !it.isUser }

        sendMessageCore(newPrompt, isRetry = hasAiBubble)
    }

    private fun sendMessageCore(promptText: String, isRetry: Boolean = false) {
        isLoading = true

        viewModelScope.launch {
            try {
                if (currentSessionId == null) {
                    currentSessionId = getHistoryUseCase.createNewSession("Transkrip ${System.currentTimeMillis()}")
                }
                val sessionId = currentSessionId!!

                if (!isRetry) {
                    messages.add(UiMessage(initialText = promptText, isUser = true))
                }

                val lastAiIndex = messages.indexOfLast { !it.isUser }
                val targetAiMessage: UiMessage

                if (isRetry && lastAiIndex != -1) {
                    val oldMsg = messages[lastAiIndex]

                    messages[lastAiIndex] = oldMsg.copy(
                        isError = false,
                        initialText = "",
                        initialIsStreaming = true
                    )
                    messages[lastAiIndex].text = ""

                    targetAiMessage = messages[lastAiIndex]
                } else {
                    val newMsg = UiMessage(initialText = "", isUser = false, initialIsStreaming = true)
                    messages.add(newMsg)
                    targetAiMessage = newMsg
                }

                summarizeUseCase(sessionId, promptText).collect { chunk ->
                    targetAiMessage.text += chunk
                }

            } catch (e: Exception) {
                val lastAiIndex = messages.indexOfLast { !it.isUser }
                if (lastAiIndex != -1) {
                    val errorMsg = messages[lastAiIndex].copy(
                        isError = true,
                        initialText = "Gagal memuat: ${e.message}"
                    )
                    errorMsg.text = "Gagal memuat: ${e.message}"
                    messages[lastAiIndex] = errorMsg
                }
            } finally {
                messages.lastOrNull { !it.isUser }?.isStreaming = false
                isLoading = false
            }
        }
    }

    private fun Message.toUiModel(): UiMessage {
        return UiMessage(
            id = this.id.toString(),
            initialText = this.text,
            isUser = this.isUser,
            isError = this.isError
        )
    }
}