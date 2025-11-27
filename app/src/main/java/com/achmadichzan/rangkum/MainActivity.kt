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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.achmadichzan.rangkum.ui.theme.RangkumTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography

class MainActivity : ComponentActivity() {
    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMediaProjectionSetup()
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
            checkAudioPermissionAndStart()
        } else {
            Toast.makeText(this, "Izin Overlay wajib!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RangkumTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = {
                        checkOverlayPermissionAndStart()
                    }) {
                        Text("Start Floating App")
                    }
                }
            }
        }
    }

    private fun checkOverlayPermissionAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            requestOverlayPermission.launch(intent)
        } else {
            checkAudioPermissionAndStart()
        }
    }

    private fun checkAudioPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED) {
            startMediaProjectionSetup()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startMediaProjectionSetup() {
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun startOverlayService(resultCode: Int, data: Intent) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = "START_RECORDING"
            putExtra("EXTRA_RESULT_CODE", resultCode)
            putExtra("EXTRA_RESULT_DATA", data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    isPreparing: Boolean = false,
    isRecording: Boolean = false,
    onStopRecording: () -> Unit = {},
    onCloseApp: () -> Unit = {},
    onWindowDrag: (Float, Float) -> Unit = { _, _ -> },
    onWindowResize: (Float, Float) -> Unit = { _, _ -> }
) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Box(modifier = modifier.padding(12.dp)) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
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
                            isPreparing -> Color.Yellow
                            isRecording -> Color.Red
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
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onCloseApp) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Gray
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        Column {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .heightIn(min = 60.dp, max = 200.dp)
                            ) {
                                val scrollState = rememberScrollState()

                                LaunchedEffect(viewModel.liveTranscript) {
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .verticalScroll(scrollState)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Live Transkrip:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (viewModel.liveTranscript.isNotBlank()) {
                                        Text(
                                            text = viewModel.liveTranscript,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = FontStyle.Italic,
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = Color.LightGray.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Mendengarkan...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onStopRecording,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
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
                            OutlinedTextField(
                                value = viewModel.userInput,
                                onValueChange = { viewModel.onInputChange(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Tanya detail...") },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
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
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(32.dp)
        )

        Icon(
            imageVector = Icons.Default.Compress,
            contentDescription = "Resize",
            tint = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(24.dp)
                .rotate(315f)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onWindowResize(dragAmount.x, dragAmount.y)
                    }
                }
        )
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