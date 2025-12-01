package com.achmadichzan.rangkum.domain.repository

import com.achmadichzan.rangkum.data.remote.model.TranscriptResponse

interface YoutubeRepository {
    suspend fun getTranscript(videoId: String): TranscriptResponse
}