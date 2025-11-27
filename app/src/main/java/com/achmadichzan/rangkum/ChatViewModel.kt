package com.achmadichzan.rangkum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    private val chatSession = model.startChat()

    val messages = mutableStateListOf<ChatMessage>()

    var userInput by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var liveTranscript by mutableStateOf("")
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

    fun sendMessage() {
        val currentMessage = userInput.trim()
        if (currentMessage.isEmpty()) return

        messages.add(ChatMessage(text = currentMessage, isUser = true))
        userInput = ""
        isLoading = true

        viewModelScope.launch {
            try {
                val response = chatSession.sendMessage(currentMessage)

                response.text?.let { responseText ->
                    messages.add(ChatMessage(text = responseText, isUser = false))
                }
            } catch (e: Exception) {
                messages.add(ChatMessage(text = "Error: ${e.localizedMessage}", isUser = false, isError = true))
            } finally {
                isLoading = false
            }
        }
    }

    fun sendTextToGemini(transcript: String) {
        isLoading = true
        messages.add(ChatMessage("Transkrip Selesai (Offline). Mengirim ke AI...", isUser = true))

        viewModelScope.launch {
            try {
                val prompt =
                    "Berikut adalah transkrip audio: \n\n\"$transcript\"\n\n " +
                            "Tolong buatkan rangkuman poin penting dari transkrip tersebut."

                val response = model.generateContent(prompt)

                response.text?.let {
                    messages.add(ChatMessage(it, isUser = false))
                }
            } catch (e: Exception) {
                messages.add(ChatMessage("Error: ${e.message}", isUser = false, isError = true))
            } finally {
                isLoading = false
            }
        }
    }
}