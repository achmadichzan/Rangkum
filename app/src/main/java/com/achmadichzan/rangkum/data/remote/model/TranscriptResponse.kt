package com.achmadichzan.rangkum.data.remote.model

import com.google.gson.annotations.SerializedName

data class TranscriptResponse(
    @SerializedName("status") val status: String,
    @SerializedName("videoId") val videoId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("transcript") val transcript: String?,
    @SerializedName("error") val error: String?
)