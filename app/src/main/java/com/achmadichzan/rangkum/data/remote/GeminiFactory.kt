package com.achmadichzan.rangkum.data.remote

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

object GeminiFactory {
    fun createModel(modelName: String): GenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName)
    }
}