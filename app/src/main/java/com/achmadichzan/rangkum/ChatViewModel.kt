package com.achmadichzan.rangkum

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.achmadichzan.rangkum.data.local.AppDatabase
import com.achmadichzan.rangkum.data.local.ChatMessageEntity
import com.achmadichzan.rangkum.data.local.ChatSession
import com.achmadichzan.rangkum.data.preferences.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).chatDao()
    private val userPreferences = UserPreferences(application)
    private var currentSessionId: Long? = null
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")
    private var chatSession = model.startChat()

    val messages = mutableStateListOf<ChatMessage>()
    var userInput by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var liveTranscript by mutableStateOf("")
        private set
    val isDarkMode = userPreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun startNewSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val newSession = ChatSession(title = "Percakapan Baru ${System.currentTimeMillis()}")
            currentSessionId = dao.insertSession(newSession)

            withContext(Dispatchers.Main) {
                messages.clear()
                liveTranscript = ""
                chatSession = model.startChat()
            }
        }
    }
    fun loadHistorySession(sessionId: Long) {
        isLoading = true
        currentSessionId = sessionId

        viewModelScope.launch(Dispatchers.IO) {
            val historyEntities = dao.getMessagesBySessionId(sessionId)

            withContext(Dispatchers.Main) {
                messages.clear()
                messages.addAll(historyEntities.map {
                    ChatMessage(it.text, it.isUser)
                })

                val geminiHistory = historyEntities.map { msg ->
                    content(role = if (msg.isUser) "user" else "model") { text(msg.text) }
                }
                chatSession = model.startChat(history = geminiHistory)

                isLoading = false
            }
        }
    }
    private fun saveMessageToDb(text: String, isUser: Boolean) {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertMessage(
                ChatMessageEntity(sessionId = sessionId, text = text, isUser = isUser)
            )
        }
    }
    fun onInputChange(newValue: String) { userInput = newValue }
    fun updateLiveTranscript(text: String) { liveTranscript = text }
    fun clearLiveTranscript() { liveTranscript = "" }
    fun sendMessage() {
        val currentMessage = userInput.trim()
        if (currentMessage.isEmpty()) return

        messages.add(ChatMessage(text = currentMessage, isUser = true))
        userInput = ""
        isLoading = true

        saveMessageToDb(currentMessage, true)

        viewModelScope.launch {
            try {
                if (currentSessionId == null) startNewSession()

                val response = chatSession.sendMessage(currentMessage)

                response.text?.let { responseText ->
                    messages.add(ChatMessage(text = responseText, isUser = false))
                    saveMessageToDb(responseText, false)
                }
            } catch (e: Exception) {
                messages.add(ChatMessage(
                    text = "Error: ${e.localizedMessage}",
                    isUser = false,
                    isError = true
                ))
            } finally {
                isLoading = false
            }
        }
    }
    fun sendTextToGemini(transcript: String) {
        isLoading = true
        messages.add(ChatMessage("Transkrip Selesai. Mengirim ke AI...", isUser = true))

        saveMessageToDb(transcript, true)

        viewModelScope.launch {
            try {
                if (currentSessionId == null) {
                    val newSession = ChatSession(title = "Transkrip ${System.currentTimeMillis()}")
                    currentSessionId = withContext(Dispatchers.IO) {
                        dao.insertSession(newSession)
                    }
                }

                val prompt = """
                    Berikut adalah transkrip mentah dari speech-to-text yang mungkin mengandung typo:
                    
                    "$transcript"
                    
                    Instruksi Khusus:
                    1. Pahami konteks dan perbaiki kesalahan ejaan/tata bahasa secara internal (dalam proses berpikirmu saja).
                    2. JANGAN tampilkan ulang teks transkrip yang diperbaiki.
                    3. Output jawabanmu harus LANGSUNG berupa Rangkuman Poin-Poin Penting dalam Bahasa Indonesia.
                """.trimIndent()

                val response = chatSession.sendMessage(prompt)

                response.text?.let {
                    messages.add(ChatMessage(it, isUser = false))
                    saveMessageToDb(it, false)
                }
            } catch (e: Exception) {
                messages.add(ChatMessage(
                    "Error: ${e.message}",
                    isUser = false,
                    isError = true
                ))
            } finally {
                isLoading = false
            }
        }
    }
}