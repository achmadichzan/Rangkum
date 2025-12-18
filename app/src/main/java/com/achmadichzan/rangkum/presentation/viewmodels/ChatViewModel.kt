package com.achmadichzan.rangkum.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.domain.model.AVAILABLE_MODELS
import com.achmadichzan.rangkum.domain.model.Message
import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.domain.model.UiVoskModel
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.domain.usecase.CreateSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.DeleteVoskModelUseCase
import com.achmadichzan.rangkum.domain.usecase.DownloadVoskModelUseCase
import com.achmadichzan.rangkum.domain.usecase.GetMessagesUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSessionUseCase
import com.achmadichzan.rangkum.domain.usecase.GetSettingsUseCase
import com.achmadichzan.rangkum.domain.usecase.GetVoskModelStatusUseCase
import com.achmadichzan.rangkum.domain.usecase.SaveMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.SummarizeTranscriptUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateMessageUseCase
import com.achmadichzan.rangkum.domain.usecase.UpdateSettingsUseCase
import com.achmadichzan.rangkum.presentation.utils.PromptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.collections.find
import kotlin.collections.map

class ChatViewModel(
    private val createSessionUseCase: CreateSessionUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val getmessagesUseCase: GetMessagesUseCase,
    private val summarizeUseCase: SummarizeTranscriptUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val downloadVoskModelUseCase: DownloadVoskModelUseCase,
    private val getVoskModelStatusUseCase: GetVoskModelStatusUseCase,
    private val deleteVoskModelUseCase: DeleteVoskModelUseCase
) : ViewModel() {
    val availableModels = listOf(
        "gemini-2.5-flash-lite" to "Flash 2.5 Lite (Cepat)",
        "gemini-2.5-flash" to "Flash 2.5 (Standar)",
        "gemini-2.5-pro" to "Pro 2.5 (Pintar)"
    )
    val currentModel = getSettingsUseCase.selectedModel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "gemini-2.5-flash-lite"
        )
    private val _voskModels = MutableStateFlow<List<UiVoskModel>>(emptyList())
    val voskModels = _voskModels.asStateFlow()
    val currentVoskModelCode = getSettingsUseCase.selectedVoskModelCode
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
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

    init {
        refreshModelList()

        viewModelScope.launch {
            currentVoskModelCode.collect { refreshModelList() }
        }
    }

    fun onInputChange(newValue: String) {
        userInput = newValue
    }

    fun updateLiveTranscript(text: String) {
        liveTranscript = text
    }

    fun clearLiveTranscript() {
        liveTranscript = ""
    }

    fun onModelChange(newModel: String) {
        viewModelScope.launch {
            updateSettingsUseCase.setModel(newModel)
        }
    }

    fun downloadModel(config: VoskModelConfig) {
        viewModelScope.launch {
            downloadVoskModelUseCase(config)
                .retry(retries = 3) { cause ->
                    if (cause is IOException) {
                        delay(1000)
                        return@retry true
                    }
                    return@retry false
                }
                .catch { e ->
                    e.printStackTrace()
                    refreshModelList()
                }
                .collect { progress ->
                    _voskModels.update { list ->
                        list.map { item ->
                            if (item.config.code == config.code) {
                                val newStatus = if (progress >= 2f) ModelStatus.READY
                                    else ModelStatus.DOWNLOADING
                                item.copy(
                                    status = newStatus,
                                    progress = if (progress > 1f) 1f else progress
                                )
                            } else item
                        }
                    }
                    if (progress >= 2f) refreshModelList()
                }
        }
    }

    fun selectVoskModel(config: VoskModelConfig) {
        viewModelScope.launch {
            updateSettingsUseCase.setVoskModel(config.code)
        }
    }

    fun deleteModel(config: VoskModelConfig) {
        viewModelScope.launch {
            try {
                deleteVoskModelUseCase(config)
                refreshModelList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            currentSessionId = createSessionUseCase(
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
            val session = getSessionUseCase(sessionId)
            if (session != null) {
                sessionTitle = session.title
            }

            val historyDomain = getmessagesUseCase(sessionId)

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
                currentSessionId = createSessionUseCase(
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

    private fun refreshModelList() {
        viewModelScope.launch {
            val currentCode = currentVoskModelCode.value
            val currentList = _voskModels.value

            val newList = AVAILABLE_MODELS.map { config ->
                async {
                    val currentItem = currentList.find { old -> old.config.code == config.code }

                    if (currentItem?.status == ModelStatus.DOWNLOADING) {
                        currentItem
                    } else {
                        val status = if (config.code == currentCode) {
                            ModelStatus.ACTIVE
                        } else {
                            getVoskModelStatusUseCase(config)
                        }
                        UiVoskModel(config, status)
                    }
                }
            }.awaitAll()

            _voskModels.value = newList
        }
    }

    private suspend fun sendMessageCore(promptText: String) {
        isLoading = true

        val errorSeparator = "\n\n> ⚠️ **Gagal Regenerate:** "

        var oldDbIdToDelete: Long? = null
        var oldTextBackup: String? = null

        try {
            val sessionId = currentSessionId!!
            val targetAiMessage: UiMessage

            val lastUserIndex = messages.indexOfLast { it.isUser }
            val expectedAiIndex = lastUserIndex + 1
            val existingAiMessage = messages.getOrNull(expectedAiIndex)?.takeIf { !it.isUser }

            if (existingAiMessage != null) {
                oldDbIdToDelete = existingAiMessage.id.toLongOrNull()
                oldTextBackup = existingAiMessage.text.split(errorSeparator)[0]

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

            if (oldDbIdToDelete != null) {
                deleteMessageUseCase(oldDbIdToDelete)
            }

        } catch (e: Exception) {
            val lastAiIndex = messages.indexOfLast { !it.isUser }

            if (oldTextBackup != null && lastAiIndex != -1) {
                val textWithError = oldTextBackup + errorSeparator + (e.message ?: "Unknown error")

                val restoredMsg = messages[lastAiIndex].copy(
                    isError = true,
                    initialText = textWithError,
                    initialIsStreaming = false
                )
                restoredMsg.text = textWithError
                messages[lastAiIndex] = restoredMsg

            } else {
                if (lastAiIndex != -1) {
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