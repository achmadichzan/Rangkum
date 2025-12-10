package com.achmadichzan.rangkum.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.domain.usecase.DeleteMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.GetHistoryUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSettingsUseCase
import com.achmadichzan.rangkum.domain.usecase.SaveMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.SummarizeTranscriptUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateSettingsUseCase
import com.achmadichzan.rangkum.presentation.utils.PromptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val summarizeUseCase: SummarizeTranscriptUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
) : ViewModel() {
    val availableModels = listOf(
        "gemini-2.5-flash-lite" to "Flash Lite (Cepat)",
        "gemini-2.5-flash" to "Flash 2.5 (Standar)",
        "gemini-2.5-pro" to "Pro 2.5 (Pintar)"
    )
    val currentModel = getSettingsUseCase.selectedModel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "gemini-2.5-flash-lite"
        )
    val messages = mutableStateListOf<UiMessage>()
    var userInput by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var liveTranscript by mutableStateOf("")
        private set
    val isDarkMode = getSettingsUseCase.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    private var currentSessionId: Long? = null
    var sessionTitle by mutableStateOf("Detail Percakapan")
        private set

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
            currentSessionId = getHistoryUseCase.createNewSession(
                "Percakapan Baru ${System.currentTimeMillis()}"
            )

            withContext(Dispatchers.Main) {
                messages.clear()
                liveTranscript = ""
            }
        }
    }

    fun loadHistorySession(sessionId: Long) {
        currentSessionId = sessionId

        viewModelScope.launch {
            val session = getHistoryUseCase.getSession(sessionId)
            if (session != null) {
                sessionTitle = session.title
            }

            val historyDomain = getHistoryUseCase.getMessages(sessionId)

            withContext(Dispatchers.Main) {
                messages.clear()
                messages.addAll(historyDomain.map { it.toUiModel() })
            }
        }
    }

    fun sendMessage() {
        val textToSend = userInput.trim()
        if (textToSend.isEmpty()) return

        messages.add(UiMessage(initialText = textToSend, isUser = true))
        userInput = ""

        viewModelScope.launch {
            try {
                if (currentSessionId == null) {
                    startNewSession()
                }
                val sessionId = currentSessionId!!
                val msgId = saveMessageUseCase(sessionId, textToSend, isUser = true)

                val lastMsgIndex = messages.indexOfLast { it.isUser }
                if (lastMsgIndex != -1) {
                    messages[lastMsgIndex] = messages[lastMsgIndex].copy(id = msgId.toString())
                }

                sendMessageCore(textToSend)

            } catch (e: Exception) {
                messages.add(
                    UiMessage(
                        initialText = "Error: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                )
            }
        }
    }

    fun sendTextToGemini(transcript: String) {
        val fullPrompt = PromptUtils.create(transcript)

        viewModelScope.launch {
            if (currentSessionId == null) {
                currentSessionId = getHistoryUseCase.createNewSession(
                    "Transkrip ${System.currentTimeMillis()}"
                )
            }
            val sessionId = currentSessionId!!

            val msgId = saveMessageUseCase(sessionId, fullPrompt, isUser = true)

            messages.add(
                UiMessage(
                    id = msgId.toString(),
                    initialText = fullPrompt,
                    isUser = true
                )
            )

            sendMessageCore(fullPrompt)
        }
    }

    fun regenerateResponse(newPrompt: String) {
        viewModelScope.launch {
            val lastUserMsg = messages.lastOrNull { it.isUser }

            if (lastUserMsg != null) {
                lastUserMsg.text = newPrompt

                val dbId = lastUserMsg.id.toLongOrNull()
                if (dbId != null) {
                    updateMessageUseCase(dbId, newPrompt)
                }
            }

            sendMessageCore(newPrompt)
        }
    }

    fun onModelChange(newModel: String) {
        viewModelScope.launch {
            updateSettingsUseCase.setModel(newModel)
        }
    }

    private suspend fun sendMessageCore(promptText: String) {
        isLoading = true

        try {
            val sessionId = currentSessionId!!
            val targetAiMessage: UiMessage

            val lastUserIndex = messages.indexOfLast { it.isUser }

            val expectedAiIndex = lastUserIndex + 1
            val existingAiMessage = messages.getOrNull(expectedAiIndex)?.takeIf { !it.isUser }

            if (existingAiMessage != null) {
                val oldDbId = existingAiMessage.id.toLongOrNull()
                if (oldDbId != null) {
                    deleteMessageUseCase(oldDbId)
                }

                val reusedMsg = existingAiMessage.copy(
                    isError = false,
                    initialText = "",
                    initialIsStreaming = true
                )
                messages[expectedAiIndex] = reusedMsg
                targetAiMessage = reusedMsg

            } else {
                val newMsg = UiMessage(
                    initialText = "",
                    isUser = false,
                    initialIsStreaming = true
                )
                messages.add(newMsg)
                targetAiMessage = newMsg
            }

            val modelName = currentModel.value

            summarizeUseCase(sessionId, promptText, modelName).collect { chunk ->
                targetAiMessage.text += chunk
            }

        } catch (e: Exception) {
            val lastAiIndex = messages.indexOfLast { !it.isUser }
            val lastUserIndex = messages.indexOfLast { it.isUser }

            if (lastAiIndex != -1 && lastAiIndex > lastUserIndex) {
                val errorMsg = messages[lastAiIndex].copy(
                    isError = true,
                    initialText = "Gagal memuat: ${e.message}"
                )
                errorMsg.text = "Gagal memuat: ${e.message}"
                messages[lastAiIndex] = errorMsg
            } else {
                messages.add(UiMessage(
                    initialText = "Error: ${e.message}",
                    isUser = false,
                    isError = true
                ))
            }
        } finally {
            messages.lastOrNull { !it.isUser }?.isStreaming = false
            isLoading = false
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