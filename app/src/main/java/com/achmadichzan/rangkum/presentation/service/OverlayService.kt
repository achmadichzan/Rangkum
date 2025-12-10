package com.achmadichzan.rangkum.presentation.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
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
import com.achmadichzan.rangkum.MainActivity
import com.achmadichzan.rangkum.domain.model.UiMessage
import com.achmadichzan.rangkum.presentation.screen.OverlayChatScreen
import com.achmadichzan.rangkum.presentation.ui.theme.RangkumTheme
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import com.achmadichzan.rangkum.presentation.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class OverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {
    private lateinit var windowManagerHelper: OverlayWindowManager
    private lateinit var audioTranscriber: AudioTranscriber
    private lateinit var composeView: ComposeView

    private val myViewModelStore by lazy { ViewModelStore() }
    override val viewModelStore: ViewModelStore get() = myViewModelStore

    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val chatViewModel: ChatViewModel by lazy {
        val factory = ViewModelFactory(applicationContext)
        ViewModelProvider(this, factory)[ChatViewModel::class.java]
    }

    private var isAudioPreparing by mutableStateOf(false)
    private var isRecording by mutableStateOf(false)
    private var isPaused by mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        startForegroundServiceNotification()

        windowManagerHelper = OverlayWindowManager(this)
        windowManagerHelper.createParams()

        audioTranscriber = AudioTranscriber(this, lifecycleScope, object : AudioTranscriber.TranscriberCallback {
            override fun onStatusUpdate(status: String) {
                chatViewModel.updateLiveTranscript(status)
            }

            override fun onLiveTranscriptUpdate(text: String) {
                chatViewModel.updateLiveTranscript(text)
            }

            override fun onFinalTranscriptResult(fullText: String) {
                if (fullText.isNotBlank()) {
                    chatViewModel.sendTextToGemini(fullText)
                }
                stopRecordingState()
            }

            override fun onError(message: String) {
                chatViewModel.messages.add(UiMessage(
                    initialText = "Error: $message",
                    isUser = false,
                    isError = true
                ))
                stopRecordingState()

                isAudioPreparing = false
            }
        })

        setupComposeView()
        windowManagerHelper.addView(composeView)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundServiceNotification()

        when (intent?.action) {
            START_RECORDING -> handleStartRecording(intent)
            ACTION_STOP -> handleStopAction()
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        windowManagerHelper.updateScreenDimensions()

        if (::composeView.isInitialized) {
            windowManagerHelper.adjustToScreenSize(composeView)
        }
    }

    private fun setupComposeView() {
        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            isFocusable = true
            isFocusableInTouchMode = true

            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    if (!windowManagerHelper.isCollapsed) {
                        windowManagerHelper.toggleCollapse(this)
                        return@setOnKeyListener true
                    }
                }
                false
            }

            setContent {
                val userPrefDark by chatViewModel.isDarkMode.collectAsState()
                val systemDark = isSystemInDarkTheme()
                val isDarkFinal = userPrefDark ?: systemDark

                RangkumTheme(darkTheme = isDarkFinal) {
                    Surface(color = Color.Transparent) {
                        OverlayChatScreen(
                            viewModel = chatViewModel,
                            isRecording = isRecording,
                            isPaused = isPaused,
                            isPreparing = isAudioPreparing,
                            isCollapsed = windowManagerHelper.isCollapsed,
                            onStartRecording = { restartRecordingProcess() },
                            onStopRecording = { audioTranscriber.stop() },
                            onTogglePause = {
                                if (isPaused) {
                                    audioTranscriber.resume()
                                    isPaused = false
                                } else {
                                    audioTranscriber.pause()
                                    isPaused = true
                                }
                                startForegroundServiceNotification()
                            },
                            onCancelRecording = { audioTranscriber.stop() },
                            onCloseApp = { performGracefulShutdown() },
                            onWindowDrag = { dx, dy ->
                                windowManagerHelper.updatePosition(composeView, dx, dy)
                            },
                            onWindowResize = { dw, dh ->
                                windowManagerHelper.resize(composeView, dw, dh)
                            },
                            onResetTranscript = {
                                chatViewModel.clearLiveTranscript()
                                audioTranscriber.clearTranscript()
                            },
                            onUpdateTranscript = { newText ->
                                chatViewModel.updateLiveTranscript(newText)
                                lifecycleScope.launch {
                                    audioTranscriber.updateInternalTranscript(newText)
                                }
                            },
                            onToggleCollapse = { windowManagerHelper.toggleCollapse(composeView) },
                            onOpacityChange = { windowManagerHelper.setOpacity(composeView, it) }
                        )
                    }
                }
            }
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "overlay_channel"
        val channel = NotificationChannel(
            channelId,
            "Floating Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val statusText = when {
            isPaused -> "Jeda (Paused)"
            isRecording -> "Sedang merekam..."
            else -> "Siap (Standby)"
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rangkum AI")
            .setContentText(statusText)
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

        startForeground(
            123,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun handleStartRecording(intent: Intent) {
        val resultCode = intent.getIntExtra("EXTRA_RESULT_CODE", 0)
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_RESULT_DATA", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_RESULT_DATA") as? Intent
        }
        val sessionId = intent.getLongExtra("EXTRA_SESSION_ID", -1L)

        if (resultCode != 0 && resultData != null) {
            if (isRecording || isAudioPreparing) {
                Log.w("OverlayService", "Permintaan start diabaikan karena sedang aktif.")
                return
            }

            if (sessionId != -1L) chatViewModel.loadHistorySession(sessionId)
            else chatViewModel.startNewSession()

            isAudioPreparing = true
            Handler(Looper.getMainLooper()).postDelayed({
                isRecording = true
                isAudioPreparing = false
                startForegroundServiceNotification()
                audioTranscriber.start(resultCode, resultData)
            }, 1000)
        }
    }

    private fun handleStopAction() {
        if (isRecording) {
            audioTranscriber.stop()
            if (windowManagerHelper.isCollapsed) {
                windowManagerHelper.toggleCollapse(composeView)
            }
        }
    }

    private fun stopRecordingState() {
        isRecording = false
        isPaused = false
        startForegroundServiceNotification()
    }

    private fun restartRecordingProcess() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun performGracefulShutdown() {
        audioTranscriber.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioTranscriber.stop()
        audioTranscriber.release()
        if (::composeView.isInitialized) {
            windowManagerHelper.removeView(composeView)
        }
        myViewModelStore.clear()
    }

    companion object {
        const val ACTION_STOP = "com.achmadichzan.rangkum.ACTION_STOP"
        const val START_RECORDING = "com.achmadichzan.rangkum.START_RECORDING"
    }
}