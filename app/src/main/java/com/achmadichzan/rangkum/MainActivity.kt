package com.achmadichzan.rangkum

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.achmadichzan.rangkum.data.local.ChatSession
import com.achmadichzan.rangkum.ui.theme.RangkumTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val sessions by viewModel.allSessions.collectAsState()
            val userPrefDark by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkFinal = userPrefDark ?: systemDark

            RangkumTheme(darkTheme = isDarkFinal) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "Rangkum AI",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                IconButton(onClick = {
                                    viewModel.toggleTheme(isDarkFinal)
                                }) {
                                    Icon(
                                        imageVector =
                                            if (isDarkFinal) Icons.Default.LightMode
                                            else Icons.Default.DarkMode,
                                        contentDescription = "Ganti Tema"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                checkOverlayPermissionAndStart(sessionId = -1L)
                            },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Add, "Chat Baru")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Text(
                            "Riwayat Rangkuman",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (sessions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Belum ada riwayat chat.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn {
                                items(sessions) { session ->
                                    HistoryItem(
                                        session = session,
                                        onClick = {
                                            checkOverlayPermissionAndStart(sessionId = session.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
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

    private var pendingSessionId: Long = -1L

    private fun startOverlayService(resultCode: Int, data: Intent) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = "START_RECORDING"
            putExtra("EXTRA_RESULT_CODE", resultCode)
            putExtra("EXTRA_RESULT_DATA", data)
            putExtra("EXTRA_SESSION_ID", pendingSessionId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
         moveTaskToBack(true)
    }
}

@Composable
fun HistoryItem(session: ChatSession, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormat.format(Date(session.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    isPreparing: Boolean = false,
    isRecording: Boolean = false,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onCancelRecording: () -> Unit = {},
    onCloseApp: () -> Unit = {},
    onWindowDrag: (Float, Float) -> Unit = { _, _ -> },
    onWindowResize: (Float, Float) -> Unit = { _, _ -> },
    onResetTranscript: () -> Unit = {},
    onUpdateTranscript: (String) -> Unit = {},
    isCollapsed: Boolean = false,
    onToggleCollapse: () -> Unit = {},
    onOpacityChange: (Float) -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    var currentAlpha by remember { mutableFloatStateOf(1f) }
    var showOpacitySlider by remember { mutableStateOf(false) }

    if (isCollapsed) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onWindowDrag(dragAmount.x, dragAmount.y)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onToggleCollapse,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                if (isRecording) {
                    Icon(
                        Icons.Default.GraphicEq,
                        "Expand",
                        Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.ChatBubble,
                        "Expand",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (isRecording) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onError, CircleShape)
                )
            }
        }
    } else {
        Box(modifier = modifier.padding(12.dp)) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onWindowDrag(dragAmount.x, dragAmount.y)
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = when {
                                isPreparing -> MaterialTheme.colorScheme.tertiary
                                isRecording -> MaterialTheme.colorScheme.error
                                else -> Color.Green
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color = statusColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            val statusText = when {
                                isPreparing -> "Menyiapkan..."
                                isRecording -> "Mendengarkan..."
                                else -> "Rangkum AI"
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            IconButton(onClick = { showOpacitySlider = !showOpacitySlider }) {
                                Icon(
                                    Icons.Default.Visibility,
                                    "Transparansi",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onToggleCollapse) {
                                Icon(
                                    Icons.Default.Minimize,
                                    "Kecilkan",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 15.dp)
                                )
                            }
                            IconButton(onClick = onCloseApp) {
                                Icon(
                                    Icons.Default.Close,
                                    "Tutup",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (showOpacitySlider) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "Transparansi: ${(currentAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = currentAlpha,
                                    onValueChange = {
                                        currentAlpha = it
                                        onOpacityChange(it)
                                    },
                                    valueRange = 0.3f..1f
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(viewModel.messages) { message ->
                            MessageBubble(message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (viewModel.isLoading) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI sedang berpikir...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isPreparing -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Menghubungkan Audio...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        isRecording -> {
                            var isEditing by remember { mutableStateOf(false) }
                            var tempEditText by remember { mutableStateOf("") }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 80.dp, max = 150.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isEditing) "Mode Edit:" else "Live Transkrip:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))

                                            if (isEditing) {
                                                BasicTextField(
                                                    value = tempEditText,
                                                    onValueChange = { tempEditText = it },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(0.dp),
                                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    ),
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                    decorationBox = { innerTextField ->
                                                        if (tempEditText.isEmpty()) {
                                                            Text(
                                                                text = "Edit transkrip...",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                )
                                            } else {
                                                val scrollState = rememberScrollState()

                                                LaunchedEffect(viewModel.liveTranscript) {
                                                    scrollState.animateScrollTo(scrollState.maxValue)
                                                }

                                                Column(
                                                    modifier = Modifier.verticalScroll(
                                                        scrollState
                                                    )
                                                ) {
                                                    if (viewModel.liveTranscript.isNotBlank()) {
                                                        Text(
                                                            text = viewModel.liveTranscript,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontStyle = FontStyle.Italic,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    } else {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        ) {
                                                            LinearProgressIndicator(
                                                                modifier = Modifier.width(60.dp)
                                                                    .height(2.dp),
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                                    alpha = 0.3f
                                                                )
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                "Mendengarkan...",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                                    alpha = 0.7f
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (isEditing) {
                                            SmallFloatingActionButton(
                                                onClick = {
                                                    onUpdateTranscript(tempEditText); isEditing =
                                                    false
                                                },
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(40.dp)
                                            ) { Icon(Icons.Default.Check, "Simpan") }
                                        } else {
                                            SmallFloatingActionButton(
                                                onClick = {
                                                    tempEditText =
                                                        viewModel.liveTranscript; isEditing = true
                                                },
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    "Edit",
                                                    Modifier.size(20.dp)
                                                )
                                            }

                                            SmallFloatingActionButton(
                                                onClick = onResetTranscript,
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    "Reset",
                                                    Modifier.size(20.dp)
                                                )
                                            }

                                            SmallFloatingActionButton(
                                                onClick = onCancelRecording,
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    "Batal",
                                                    Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = onStopRecording,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Stop, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop & Rangkum")
                                }
                            }
                        }

                        else -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = onStartRecording,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.GraphicEq, "Rekam Lagi")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = viewModel.userInput,
                                    onValueChange = { viewModel.onInputChange(it) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Tanya detail...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { viewModel.sendMessage() },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, "Kirim")
                                }
                            }
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag Handle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(bottom = 8.dp)
                    .size(32.dp)
            )

            Icon(
                imageVector = Icons.Default.NorthWest,
                contentDescription = "Resize",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onWindowResize(dragAmount.x, dragAmount.y)
                        }
                    }

                    .rotate(180f)
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val bubbleColor =
        if (message.isUser) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.secondaryContainer

    val textColor =
        if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSecondaryContainer

    val alignment = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = if (message.isError) MaterialTheme.colorScheme.errorContainer else bubbleColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Markdown(
                content = message.text,
                colors = MarkdownDefaults.markdownColors(
                    textColor = textColor,
                    codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                typography = MarkdownDefaults.markdownTypography(
                    body1 = MaterialTheme.typography.bodyMedium,
                    code = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

object MarkdownDefaults {
    @Composable
    fun markdownColors(
        textColor: Color = Color.Unspecified,
        codeBackgroundColor: Color = Color.Unspecified,
    ): MarkdownColors {
        return DefaultMarkdownColors(
            text = textColor,
            codeBackground = codeBackgroundColor,
            inlineCodeBackground = codeBackgroundColor,
            dividerColor = MaterialTheme.colorScheme.outlineVariant,
            tableBackground = Color.Transparent
        )
    }

    @Composable
    fun markdownTypography(
        body1: TextStyle,
        code: TextStyle
    ): MarkdownTypography {
        return DefaultMarkdownTypography(
            h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            h3 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            h4 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),

            text = body1,
            paragraph = body1,
            code = code,
            inlineCode = code,
            quote = body1.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            ),
            ordered = body1,
            bullet = body1,
            list = body1,

            textLink = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ),

            table = body1
        )
    }
}