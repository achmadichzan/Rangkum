package com.achmadichzan.rangkum.data.repository

import com.achmadichzan.rangkum.BuildConfig
import com.achmadichzan.rangkum.data.remote.model.TranscriptResponse
import com.achmadichzan.rangkum.domain.repository.YoutubeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class YoutubeRepositoryImpl(
    private val client: HttpClient,
    private val ioDispatcher: CoroutineDispatcher
) : YoutubeRepository {
    private val baseUrl = BuildConfig.BASE_URL

    override suspend fun getTranscript(videoId: String): TranscriptResponse {
        return withContext(ioDispatcher) {
            try {
                client.get("$baseUrl/transcript") {
                    parameter("videoId", videoId)
                }.body()
            } catch (e: Exception) {
                throw e
            }
        }
    }
}