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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
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
    private var currentModelPath: String? = null
    private var voskRecognizer: Recognizer? = null
    private var recordingJob: Job? = null
    private var processingJob: Job? = null
    private var isRecording = false
    private val finalTranscript = StringBuilder()
    private var isIntentionalStop = false
    private var isSilentStop = false
    private val transcriberMutex = Mutex()
    private val isPaused = AtomicBoolean(false)
    private val RECORDING_SAMPLE_RATE = 48000
    private val VOSK_SAMPLE_RATE = 16000.0f
    private var audioChannel = Channel<ByteArray>(Channel.UNLIMITED)
    private var mediaProjectionCallback: MediaProjection.Callback? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start(resultCode: Int, resultData: Intent, modelPath: String) {
        scope.launch(Dispatchers.IO) {
            isPaused.store(false)

            reset()

            isSilentStop = false
            isIntentionalStop = false

            withContext(Dispatchers.Main) {
                callback.onStatusUpdate("Menyiapkan Recorder...")
            }

            try {
                val projectionManager =
                    context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

                // Simpan callback ke variabel agar bisa dibersihkan nanti
                mediaProjectionCallback = object : MediaProjection.Callback() {
                    override fun onStop() {
                        if (!isIntentionalStop) {
                            super.onStop()
                            stop() // Stop aman memanggil audioRecord?.stop()
                            callback.onError("MediaProjection dihentikan sistem.")
                        }
                    }
                }

                mediaProjection?.registerCallback(
                    mediaProjectionCallback!!, Handler(Looper.getMainLooper())
                )

                if (mediaProjection == null) {
                    withContext(Dispatchers.Main) { callback.onError("Gagal init MediaProjection") }
                    return@launch
                }

                val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                    .build()

                val minSystemBuffer = AudioRecord.getMinBufferSize(
                    RECORDING_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val finalBufferSize = maxOf(minSystemBuffer * 4, 64 * 1024)

                audioRecord = AudioRecord.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(RECORDING_SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                            .build())
                    .setBufferSizeInBytes(finalBufferSize)
                    .setAudioPlaybackCaptureConfig(config)
                    .build()

                audioChannel = Channel(Channel.UNLIMITED)

                if (voskModel == null || currentModelPath != modelPath) {
                    withContext(Dispatchers.Main) {
                        callback.onStatusUpdate("Memuat Model...")
                    }
                    voskModel?.close()
                    voskModel = Model(modelPath)
                    currentModelPath = modelPath
                }

                voskRecognizer = Recognizer(voskModel, VOSK_SAMPLE_RATE)

                withContext(Dispatchers.Main) {
                    callback.onStatusUpdate("")
                }

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.startRecording()
                    isRecording = true

                    startRecordingLoop(audioRecord!!)
                    startProcessingLoop()
                } else {
                    throw IllegalStateException("AudioRecord gagal inisialisasi.")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Start Gagal: ${e.message}")
                }
                reset()
            }
        }
    }

    private fun startRecordingLoop(record: AudioRecord) {
        recordingJob = scope.launch(Dispatchers.IO) {
            val bufferSize = 4800
            val buffer = ByteArray(bufferSize)

            try {
                while (isRecording) {
                    ensureActive()
                    val read = record.read(
                        buffer,
                        0,
                        buffer.size,
                        AudioRecord.READ_BLOCKING
                    )

                    if (read < 0) break
                    if (read > 0 && !isPaused.load()) {
                        val dataCopy = buffer.copyOfRange(0, read)
                        audioChannel.trySend(dataCopy)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioChannel.close()
            }
        }
    }

    private fun startProcessingLoop() {
        finalTranscript.clear()

        processingJob = scope.launch(Dispatchers.Default) {
            val voskBuffer = ByteArray(4096)

            try {
                for (rawAudio in audioChannel) {
                    ensureActive()

                    if (rawAudio.size < 6) continue

                    val validLen = (rawAudio.size / 6) * 6
                    val convertedSize = resample48kTo16k(rawAudio, validLen, voskBuffer)

                    if (convertedSize == 0) continue

                    transcriberMutex.withLock {
                        val recognizer = voskRecognizer
                        if (recognizer != null) {
                            if (recognizer.acceptWaveForm(voskBuffer, convertedSize)) {
                                val text = parseVoskResult(recognizer.result)
                                if (text.isNotEmpty()) {
                                    finalTranscript.append(text).append(" ")
                                    withContext(Dispatchers.Main) {
                                        callback.onLiveTranscriptUpdate(finalTranscript.toString())
                                    }
                                }
                            } else {
                                val partial = parseVoskPartial(recognizer.partialResult)
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
                if (!isSilentStop) {
                    finishProcessing()
                }
            }
        }
    }

    private suspend fun finishProcessing() {
        if (!scope.isActive) return

        transcriberMutex.withLock {
            if (!isSilentStop && voskRecognizer != null && voskModel != null) {
                try {
                    val finalResult = voskRecognizer!!.finalResult
                    val finalText = parseVoskResult(finalResult)

                    if (finalText.isNotEmpty()) {
                        finalTranscript.append(finalText)
                    }

                    val fullText = finalTranscript.toString()

                    if (fullText.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            callback.onLiveTranscriptUpdate(fullText)
                            callback.onFinalTranscriptResult(fullText)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun resample48kTo16k(input: ByteArray, length: Int, targetBuffer: ByteArray): Int {
        var outputIndex = 0
        var i = 0
        while (i < length - 5) {
            targetBuffer[outputIndex] = input[i]
            targetBuffer[outputIndex + 1] = input[i + 1]
            outputIndex += 2

            i += 6
        }
        return outputIndex
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
        scope.launch {
            transcriberMutex.withLock {
                finalTranscript.clear()
                voskRecognizer?.reset()
            }
        }
    }

    fun pause() {
        isPaused.store(true)
    }

    fun resume() {
        isPaused.store(false)
    }

    fun stop() {
        isIntentionalStop = true
        isRecording = false
        isPaused.store(false)
        try { audioRecord?.stop() } catch (e: Exception) { e.printStackTrace() }
    }

    fun cancel() {
        isIntentionalStop = true
        isSilentStop = true
        isRecording = false
        isPaused.store(false)

        try { audioRecord?.stop() } catch (e: Exception) { e.printStackTrace() }

        recordingJob?.cancel()
        processingJob?.cancel()
    }

    fun release() {
        stop()
        recordingJob?.cancel()
        processingJob?.cancel()

        scope.launch(Dispatchers.IO) {
            reset()
            transcriberMutex.withLock {
                voskRecognizer?.close()
                voskRecognizer = null

                voskModel?.close()
                voskModel = null
                currentModelPath = null
            }
        }
    }

    suspend fun reset() {
        isIntentionalStop = true
        isSilentStop = true
        isRecording = false

        recordingJob?.cancel()
        processingJob?.cancel()

        voskRecognizer?.reset()
        cleanupResources()
    }

    private suspend fun cleanupResources() {
        transcriberMutex.withLock {
            try {
                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    try { audioRecord?.stop() } catch (e: Exception) { e.printStackTrace() }
                }
                audioRecord?.release()

                mediaProjectionCallback?.let { callback ->
                    mediaProjection?.unregisterCallback(callback)
                }
                mediaProjectionCallback = null

                mediaProjection?.stop()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioRecord = null
                mediaProjection = null
            }
        }
    }
}