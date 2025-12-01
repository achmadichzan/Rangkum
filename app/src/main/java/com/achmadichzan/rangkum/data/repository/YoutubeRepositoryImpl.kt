package com.achmadichzan.rangkum.data.repository

import com.achmadichzan.rangkum.data.remote.model.TranscriptResponse
import com.achmadichzan.rangkum.domain.repository.YoutubeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class YoutubeRepositoryImpl(
    private val client: HttpClient
) : YoutubeRepository {
    private val baseUrl = "https://rangkum-service-340248347048.asia-southeast2.run.app"

    override suspend fun getTranscript(videoId: String): TranscriptResponse {
        return client.get("$baseUrl/transcript") {
            parameter("videoId", videoId)
        }.body()
    }
}