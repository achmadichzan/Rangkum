package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.data.remote.model.TranscriptResponse
import com.achmadichzan.rangkum.domain.repository.YoutubeRepository
import java.util.regex.Pattern

class GetYoutubeTranscriptUseCase(private val repository: YoutubeRepository) {
    suspend operator fun invoke(url: String): TranscriptResponse {
        val videoId = extractVideoId(url)
            ?: throw IllegalArgumentException("Link YouTube tidak valid")
        val response = repository.getTranscript(videoId)

        if (response.status == "success" && !response.transcript.isNullOrBlank()) {
            return response
        } else {
            throw Exception(response.error ?: "Gagal mengambil subtitle")
        }
    }

    private fun extractVideoId(url: String): String? {
        val pattern = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/\\w\\/)|(embed\\/)|(watch\\?)\\??v?=?|(&?v=)|(shorts\\/)|(live\\/))([^#\\&\\?]*).*"

        val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = compiledPattern.matcher(url)

        if (matcher.matches()) {
            val id = matcher.group(10) // Ambil grup capture terakhir (ID)
            return if (!id.isNullOrBlank() && id.length == 11) id else null
        }
        return null
    }
}