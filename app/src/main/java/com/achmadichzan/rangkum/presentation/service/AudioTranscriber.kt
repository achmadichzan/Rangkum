package com.achmadichzan.rangkum.presentation.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService

class AudioTranscriber(
    private val context: Context,
    private val scope: CoroutineScope,
    private val callback: TranscriberCallback
) {
    interface TranscriberCallback {
        fun onStatusUpdate(status: String)
        fun onLiveTranscriptUpdate(text: String)
        fun onFinalTranscriptResult(fullText: String)
        fun onError(message: String)
    }

    private var audioRecord: AudioRecord? = null
    private var mediaProjection: MediaProjection? = null
    private var voskModel: Model? = null
    private var voskRecognizer: Recognizer? = null
    private var transcriptionJob: Job? = null
    private var isRecording = false
    private val finalTranscript = StringBuilder()
    private var isIntentionalStop = false
    private var isSilentStop = false
    private val transcriberMutex = Mutex()

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start(resultCode: Int, resultData: Intent) {
        reset()
        isSilentStop = false
        isIntentionalStop = false

        callback.onStatusUpdate("Menyiapkan Recorder...")

        try {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    if (!isIntentionalStop) {
                        super.onStop()
                        stop()
                        callback.onError("MediaProjection dihentikan sistem.")
                    }
                }
            }, Handler(Looper.getMainLooper()))

            if (mediaProjection == null) {
                callback.onError("Gagal init MediaProjection")
                return
            }

            val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            val sampleRate = 16000
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val bufferSize = maxOf(minBufferSize, 12288)

            audioRecord = AudioRecord.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .setAudioPlaybackCaptureConfig(config)
                .build()

            audioRecord?.startRecording()
            isRecording = true
            startProcessingLoop(audioRecord!!)

        } catch (e: Exception) {
            callback.onError("Start Gagal: ${e.message}")
            stop()
        }
    }

    private fun startProcessingLoop(record: AudioRecord) {
        finalTranscript.clear()

        transcriptionJob = scope.launch(Dispatchers.IO) {
            if (voskModel == null) {
                try {
                    withContext(Dispatchers.Main) { callback.onStatusUpdate("Memuat Model...") }
                    val modelPath = StorageService.sync(context, "model-en", "model")
                    voskModel = Model(modelPath)
                    voskRecognizer = Recognizer(voskModel, 16000.0f)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { callback.onError("Gagal Load Model: ${e.message}") }
                    return@launch
                }
            }

            voskRecognizer?.reset()
            val buffer = ByteArray(4096)

            withContext(Dispatchers.Main) {
                callback.onLiveTranscriptUpdate("")
            }

            transcriberMutex.withLock {
                voskRecognizer?.reset()
            }

            try {
                while (isRecording) {
                    try { ensureActive() } catch(e: Exception) { e.printStackTrace(); break }
                    val read = record.read(buffer, 0, buffer.size)

                    if (read < 0) { break }
                    if (read > 0) {
                        transcriberMutex.withLock {
                            if (voskRecognizer!!.acceptWaveForm(buffer, read)) {
                                val text = parseVoskResult(voskRecognizer!!.result)
                                if (text.isNotEmpty()) {
                                    finalTranscript.append(text).append(" ")
                                    withContext(Dispatchers.Main) {
                                        callback.onLiveTranscriptUpdate(finalTranscript.toString())
                                    }
                                }
                            } else {
                                val partial = parseVoskPartial(voskRecognizer!!.partialResult)
                                if (partial.isNotEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        callback.onLiveTranscriptUpdate("$finalTranscript $partial")
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                transcriberMutex.withLock {
                    if (!isSilentStop) {
                        if (voskRecognizer != null) {
                            val finalResult = voskRecognizer!!.finalResult
                            val finalText = parseVoskResult(finalResult)
                            finalTranscript.append(finalText)
                        }

                        val fullText = finalTranscript.toString()

                        withContext(Dispatchers.Main) {
                            callback.onLiveTranscriptUpdate(fullText)
                            callback.onFinalTranscriptResult(fullText)
                        }
                    }
                }
                cleanupResources()
            }
        }
    }

    suspend fun updateInternalTranscript(newText: String) {
        transcriberMutex.withLock {
            finalTranscript.clear()
            finalTranscript.append(newText)
            voskRecognizer?.reset()
        }
    }

    private fun parseVoskResult(json: String): String =
        JSONObject(json).optString("text", "")
    private fun parseVoskPartial(json: String): String =
        JSONObject(json).optString("partial", "")

    fun clearTranscript() {
        finalTranscript.clear()
        voskRecognizer?.reset()
    }

    fun stop() {
        isIntentionalStop = true
        isRecording = false

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stop()
        transcriptionJob?.cancel()
        cleanupResources()
    }

    fun reset() {
        isSilentStop = true
        isRecording = false

        try {
            audioRecord?.stop()
            mediaProjection?.stop()
        } catch (e: Exception) { e.printStackTrace() }

        transcriptionJob?.cancel()
        cleanupResources()
    }

    private fun cleanupResources() {
        try {
            audioRecord?.release()
            mediaProjection?.stop()
        } catch (e: Exception) { e.printStackTrace() }

        audioRecord = null
        mediaProjection = null
    }
}