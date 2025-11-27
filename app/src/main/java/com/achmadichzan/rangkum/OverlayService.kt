package com.achmadichzan.rangkum

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService

class OverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView

    private val myViewModelStore by lazy { ViewModelStore() }
    override val viewModelStore: ViewModelStore
        get() = myViewModelStore

    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var isPreparing by mutableStateOf(false)
    private var isRecording by mutableStateOf(false)
    private var audioRecordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private var voskModel: Model? = null
    private var voskRecognizer: Recognizer? = null
    private val finalTranscript = StringBuilder()

    private fun resetTranscript() {
        finalTranscript.clear()

        val viewModel = ViewModelProvider(this@OverlayService)[ChatViewModel::class.java]
        viewModel.clearLiveTranscript()
    }

    private fun updateTranscriptManual(newText: String) {
        finalTranscript.clear()
        finalTranscript.append(newText)

        val viewModel = ViewModelProvider(this@OverlayService)[ChatViewModel::class.java]
        viewModel.updateLiveTranscript(newText)
    }

    private fun restartRecordingProcess() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()

        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val metrics = resources.displayMetrics
        val initialWidth = (300 * metrics.density).toInt()
        val initialHeight = (400 * metrics.density).toInt()

        val minWidth = (200 * metrics.density).toInt()
        val minHeight = (150 * metrics.density).toInt()

        val params = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.y = 200

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                Surface(color = Color.Transparent) {
                    ChatScreen(
                        isRecording = isRecording,
                        isPreparing = isPreparing,
                        onStartRecording = {
                            restartRecordingProcess()
                        },
                        onStopRecording = { stopRecording() },
                        onCloseApp = {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        },
                        onWindowDrag = { deltaX, deltaY ->
                            params.x += deltaX.toInt()
                            params.y += deltaY.toInt()
                            try {
                                windowManager.updateViewLayout(composeView, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onWindowResize = { deltaWidth, deltaHeight ->
                            val newWidth = params.width + deltaWidth.toInt()
                            val newHeight = params.height + deltaHeight.toInt()

                            params.width = newWidth.coerceAtLeast(minWidth)
                            params.height = newHeight.coerceAtLeast(minHeight)

                            updateWindow(params)
                        },
                        onResetTranscript = { resetTranscript() },
                        onUpdateTranscript = { newText -> updateTranscriptManual(newText) }
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            Log.e("OverlayService", "Gagal Overlay: ${e.message}")
            stopSelf()
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForegroundServiceNotification()

        if (intent?.action == "START_RECORDING") {
            val resultCode = intent.getIntExtra("EXTRA_RESULT_CODE", 0)
            val resultData = intent.getParcelableExtra<Intent>("EXTRA_RESULT_DATA")

            if (resultCode != 0 && resultData != null) {
                isPreparing = true

                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        startForegroundServiceNotification()
                        setupInternalRecorder(resultCode, resultData)
                    } catch (e: Exception) {
                        Log.e("OverlayService", "Gagal start: ${e.message}")
                        isPreparing = false
                        try {
                            setupInternalRecorder(resultCode, resultData)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            stopSelf()
                        }
                    }
                }, 1000)
            }
        }
        return START_STICKY
    }

    private fun updateWindow(params: WindowManager.LayoutParams) {
        try {
            windowManager.updateViewLayout(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rangkum AI")
            .setContentText("Siap merekam audio system...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                123,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(123, notification)
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun setupInternalRecorder(resultCode: Int, resultData: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
            if (mediaProjection == null) {
                Log.e("OverlayService", "MediaProjection gagal (null)")
                isPreparing = false
                return
            }

            val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            val audioRecord = AudioRecord.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setAudioPlaybackCaptureConfig(config)
                .setBufferSizeInBytes(minBufferSize)
                .build()

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("OverlayService", "AudioRecord gagal init (State Uninitialized)")
                isPreparing = false
                return
            }

            this.audioRecord = audioRecord

            try {
                this.audioRecord?.startRecording()
                startProcessingAudio(this.audioRecord!!)
            } catch (e: Exception) {
                Log.e("OverlayService", "Gagal startRecording: ${e.message}")
                isPreparing = false
            }

        } else {
            isPreparing = false
        }
    }

    private fun startProcessingAudio(record: AudioRecord) {
        isPreparing = false
        isRecording = true
        finalTranscript.clear()

        val viewModel = ViewModelProvider(this@OverlayService)[ChatViewModel::class.java]

        viewModel.updateLiveTranscript("Mulai mendengarkan...")

        audioRecordingJob = lifecycleScope.launch(Dispatchers.IO) {

            if (voskModel == null) {
                try {
                    withContext(Dispatchers.Main) {
                        viewModel.updateLiveTranscript("Memuat Model AI...")
                    }
                    initVoskModelSync()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewModel.updateLiveTranscript("Gagal load model: ${e.message}")
                        stopRecording()
                    }
                    return@launch
                }
            }

            if (voskRecognizer == null && voskModel != null) {
                voskRecognizer = Recognizer(voskModel, 44100.0f)
            }
            voskRecognizer?.reset()

            withContext(Dispatchers.Main) {
                viewModel.updateLiveTranscript("Mendengarkan...")
            }

            val bufferSize = 8192
            val buffer = ByteArray(bufferSize)
            var lastUiUpdateTime = 0L
            val uiUpdateInterval = 150L

            try {
                while (isRecording) {
                    val readResult = record.read(buffer, 0, buffer.size)

                    if (readResult > 0 && voskRecognizer != null) {
                        val isFinal = voskRecognizer!!.acceptWaveForm(buffer, readResult)

                        if (isFinal) {
                            val resultJson = voskRecognizer!!.result
                            val text = parseVoskResult(resultJson)

                            if (text.isNotEmpty()) {
                                finalTranscript.append(text).append(" ")

                                val currentFullText = finalTranscript.toString()
                                withContext(Dispatchers.Main) {
                                    viewModel.updateLiveTranscript(currentFullText)
                                }
                            }
                        } else {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastUiUpdateTime > uiUpdateInterval) {

                                val partialJson = voskRecognizer!!.partialResult
                                val partialText = parseVoskPartial(partialJson)

                                if (partialText.isNotEmpty()) {
                                    val displayText = "$finalTranscript $partialText"
                                    withContext(Dispatchers.Main) {
                                        viewModel.updateLiveTranscript(displayText)
                                    }
                                    lastUiUpdateTime = currentTime
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (voskRecognizer != null) {
                    val finalResult = voskRecognizer!!.finalResult
                    val text = parseVoskResult(finalResult)
                    finalTranscript.append(text)
                }

                val fullText = finalTranscript.toString()
                withContext(Dispatchers.Main) {
                    viewModel.updateLiveTranscript(fullText)
                    if (fullText.isNotBlank()) {
                        viewModel.sendTextToGemini(fullText)
                    }
                }
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
    }

    private fun initVoskModelSync() {
        val modelPath = StorageService.sync(this, "model-en", "model")

        this.voskModel = Model(modelPath)
        this.voskRecognizer = Recognizer(voskModel, 44100.0f)

        Log.d("OverlayService", "Vosk Model berhasil dimuat secara sinkron!")
    }

    private fun parseVoskResult(json: String): String {
        return try {
            val jsonObj = org.json.JSONObject(json)
            jsonObj.optString("text", "")
        } catch (e: Exception) {
            e.printStackTrace()
            "Error parseVoskResult JSON"
        }
    }

    private fun parseVoskPartial(json: String): String {
        return try {
            val jsonObj = org.json.JSONObject(json)
            jsonObj.optString("partial", "")
        } catch (e: Exception) {
            e.printStackTrace()
            "Error parseVoskPartial JSON"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
        myViewModelStore.clear()
    }
}