package com.achmadichzan.rangkum

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.achmadichzan.rangkum.presentation.screen.MainScreen
import com.achmadichzan.rangkum.presentation.service.OverlayService

class MainActivity : ComponentActivity() {
    private var keepSplash = true
    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    private var pendingSessionId: Long = -1L
    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMediaProjectionSetup(pendingSessionId)
        } else {
            Toast.makeText(
                this,
                "Izin Audio wajib untuk merekam suara sistem",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val startMediaProjection = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            startOverlayService(result.resultCode, result.data!!)
        }
    }
    private val requestOverlayPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            checkAudioPermissionAndStart(pendingSessionId)
        } else {
            Toast.makeText(
                this,
                "Izin Overlay wajib!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        setContent {
            MainScreen { sessionId ->
                checkOverlayPermissionAndStart(sessionId)
            }
        }
        keepSplash = false
    }

    private fun checkOverlayPermissionAndStart(sessionId: Long) {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            requestOverlayPermission.launch(intent)
        } else {
            checkAudioPermissionAndStart(sessionId)
        }
    }

    private fun checkAudioPermissionAndStart(sessionId: Long) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED) {
            startMediaProjectionSetup(sessionId)
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startMediaProjectionSetup(sessionId: Long) {
        this.pendingSessionId = sessionId
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun startOverlayService(resultCode: Int, data: Intent) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.START_RECORDING
            putExtra("EXTRA_RESULT_CODE", resultCode)
            putExtra("EXTRA_RESULT_DATA", data)
            putExtra("EXTRA_SESSION_ID", pendingSessionId)
        }

        startForegroundService(intent)
        moveTaskToBack(true)
    }
}