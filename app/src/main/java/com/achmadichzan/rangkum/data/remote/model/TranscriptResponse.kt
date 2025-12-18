package com.achmadichzan.rangkum.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranscriptResponse(
    @SerialName("detected_language") val detectedLanguage: String? = null,
    @SerialName("status") val status: String,
    @SerialName("videoId") val videoId: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("channel") val channel: String? = null,
    @SerialName("transcript") val transcript: String? = null,
    @SerialName("error") val error: String? = null
)