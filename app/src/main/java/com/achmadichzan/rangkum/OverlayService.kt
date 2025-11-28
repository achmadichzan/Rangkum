package com.achmadichzan.rangkum

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
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
import com.achmadichzan.rangkum.ui.theme.RangkumTheme
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

    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ChatViewModel::class.java]
    }

    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var isPreparing by mutableStateOf(false)
    private var isRecording by mutableStateOf(false)
    private var isCancelled = false
    private var audioRecordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private var mediaProjection: MediaProjection? = null

    private var voskModel: Model? = null
    private var voskRecognizer: Recognizer? = null
    private val finalTranscript = StringBuilder()

    private lateinit var params: WindowManager.LayoutParams

    private var lastWidth = 0
    private var lastHeight = 0
    private var isCollapsed by mutableStateOf(false)

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
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val initialWidth = (300 * metrics.density).toInt()
        val initialHeight = (400 * metrics.density).toInt()

        val minWidth = (200 * metrics.density).toInt()
        val minHeight = (150 * metrics.density).toInt()

        lastWidth = initialWidth
        lastHeight = initialHeight

        params = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,

            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.y = 200

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            isFocusable = true
            isFocusableInTouchMode = true

            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    if (!isCollapsed) {
                        toggleCollapse()
                        return@setOnKeyListener true
                    }

                }
                false
            }

            setContent {
                val userPrefDark by chatViewModel.isDarkMode.collectAsState()
                val systemDark = isSystemInDarkTheme()
                val isDarkFinal = userPrefDark ?: systemDark

                RangkumTheme(darkTheme = isDarkFinal, dynamicColor = false) {
                    Surface(color = Color.Transparent) {
                        ChatScreen(
                            isRecording = isRecording,
                            isPreparing = isPreparing,
                            onStartRecording = {
                                restartRecordingProcess()
                            },
                            onStopRecording = { stopRecording() },
                            onCancelRecording = { cancelRecording() },
                            onCloseApp = { performGracefulShutdown() },
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
                                val rawWidth = params.width + deltaWidth.toInt()
                                val rawHeight = params.height + deltaHeight.toInt()

                                val maxWidthAllowed = screenWidth - params.x
                                val maxHeightAllowed = screenHeight - params.y

                                params.width = rawWidth.coerceIn(minWidth, maxWidthAllowed)
                                params.height = rawHeight.coerceIn(minHeight, maxHeightAllowed)

                                if (!isCollapsed) {
                                    lastWidth = params.width
                                    lastHeight = params.height
                                }
                                updateWindow(params)
                            },
                            onResetTranscript = { resetTranscript() },
                            onUpdateTranscript = { newText -> updateTranscriptManual(newText) },
                            isCollapsed = isCollapsed,
                            onToggleCollapse = { toggleCollapse() },
                            onOpacityChange = { newAlpha -> updateOpacity(newAlpha) }
                        )
                    }
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

        if (intent?.action == START_RECORDING) {
            val resultCode = intent.getIntExtra("EXTRA_RESULT_CODE", 0)
            val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("EXTRA_RESULT_DATA", Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("EXTRA_RESULT_DATA") as? Intent
            }
            val sessionId = intent.getLongExtra("EXTRA_SESSION_ID", -1L)

            if (resultCode != 0 && resultData != null) {
                isPreparing = true

                val viewModel = ViewModelProvider(
                    this@OverlayService,
                    ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )[ChatViewModel::class.java]

                if (sessionId != -1L) {
                    viewModel.loadHistorySession(sessionId)
                } else {
                    viewModel.startNewSession()
                }

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
        if (intent?.action == ACTION_STOP) {
            if (isRecording) {
                stopRecording()

                if (isCollapsed) {
                    toggleCollapse()
                }
            }
            return START_NOT_STICKY
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
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rangkum AI")
            .setContentText(if (isRecording) "Sedang merekam..." else "Siap (Standby)")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (isRecording) {
            notificationBuilder.addAction(
                android.R.drawable.ic_media_pause,
                "STOP & RANGKUM",
                stopPendingIntent
            )
        }
        val notification = notificationBuilder.build()

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

            this.mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
            this.mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    Log.w("OverlayService", "MediaProjection dihentikan oleh sistem/aplikasi lain!")

                    stopRecording()
                    Toast.makeText(
                        this@OverlayService,
                        "MediaProjection dihentikan oleh sistem/aplikasi lain!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Handler(Looper.getMainLooper()))

            if (this.mediaProjection == null) {
                Log.e("OverlayService", "MediaProjection gagal (null)")
                isPreparing = false
                return
            }

            val config = AudioPlaybackCaptureConfiguration.Builder(this.mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            val sampleRate = 16000
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
                cleanupResources()
                isPreparing = false
            }

        } else {
            isPreparing = false
        }
    }

    private fun startProcessingAudio(record: AudioRecord) {
        isPreparing = false
        isRecording = true
        isCancelled = false

        startForegroundServiceNotification()

        finalTranscript.clear()

        chatViewModel.clearLiveTranscript()
        chatViewModel.updateLiveTranscript("Mulai mendengarkan...")

        audioRecordingJob = lifecycleScope.launch(Dispatchers.IO) {

            if (voskModel == null) {
                try {
                    withContext(Dispatchers.Main) {
                        chatViewModel.updateLiveTranscript("Memuat Model AI...")
                    }
                    initVoskModelSync()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        chatViewModel.updateLiveTranscript("Gagal load model: ${e.message}")
                        stopRecording()
                    }
                    return@launch
                }
            }

            if (voskRecognizer == null && voskModel != null) {
                voskRecognizer = Recognizer(voskModel, 16000.0f)
            }
            voskRecognizer?.reset()

            withContext(Dispatchers.Main) {
                chatViewModel.updateLiveTranscript("")
            }

            val bufferSize = 12288
            val buffer = ByteArray(bufferSize)
            var lastUiUpdateTime = 0L
            val uiUpdateInterval = 200L

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
                                    chatViewModel.updateLiveTranscript(currentFullText)
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
                                        chatViewModel.updateLiveTranscript(displayText)
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
                    chatViewModel.updateLiveTranscript(fullText)

                    if (!isCancelled && fullText.isNotBlank()) {
                        chatViewModel.sendTextToGemini(fullText)
                    } else if (isCancelled) {
                        chatViewModel.updateLiveTranscript("")
                        chatViewModel.messages.add(ChatMessage("Rekaman dibatalkan.", true))
                    }
                }

                cleanupResources()
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        isCancelled = false

        startForegroundServiceNotification()
    }

    private fun cancelRecording() {
        isCancelled = true
        isRecording = false
    }

    private fun initVoskModelSync() {
        val modelPath = StorageService.sync(this, "model-en", "model")

        this.voskModel = Model(modelPath)
        this.voskRecognizer = Recognizer(voskModel, 16000.0f)

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

    private fun updateOpacity(alpha: Float) {
        params.alpha = alpha
        updateWindow(params)
    }

    private fun toggleCollapse() {
        val metrics = resources.displayMetrics
        val bubbleSize = (60 * metrics.density).toInt()

        if (isCollapsed) {
            params.width = lastWidth
            params.height = lastHeight

            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()

            isCollapsed = false
        } else {
            lastWidth = params.width
            lastHeight = params.height

            params.width = bubbleSize
            params.height = bubbleSize

            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

            isCollapsed = true
        }

        updateWindow(params)
    }

    private fun performGracefulShutdown() {
        stopForeground(STOP_FOREGROUND_REMOVE)

        if (isRecording) {
            isRecording = false
        }

        cleanupResources()

        stopSelf()
    }

    private fun cleanupResources() {
        try {
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord?.stop()
                }
            }
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
        }

        try {
            mediaProjection?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaProjection = null
        }

        isRecording = false
        isPreparing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
        myViewModelStore.clear()
    }

    companion object {
        const val ACTION_STOP = "com.achmadichzan.rangkum.ACTION_STOP"
        const val START_RECORDING = "com.achmadichzan.rangkum.START_RECORDING"
    }
}