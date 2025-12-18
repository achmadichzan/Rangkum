package com.achmadichzan.rangkum.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.UiVoskModel
import com.achmadichzan.rangkum.domain.model.VoskModelConfig

@Composable
fun LanguageSelectionDialog(
    models: List<UiVoskModel>,
    onDismiss: () -> Unit,
    onDownload: (VoskModelConfig) -> Unit,
    onSelect: (VoskModelConfig) -> Unit,
    onDelete: (VoskModelConfig) -> Unit,
    onConfirm: (() -> Unit)? = null
) {
    AlertDialog(
        modifier = Modifier.heightIn(max = 400.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(if (onConfirm != null) "Mulai Chat Baru" else "Pilih Bahasa Transkripsi")
        },
        text = {
            LazyColumn(
                modifier = Modifier.widthIn(max = 400.dp)
            ) {
                items(models) { item ->
                    LanguageItemRow(
                        item = item,
                        onDownload = onDownload,
                        onSelect = { onSelect(it) },
                        onDelete = { onDelete(it) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        },
        confirmButton = {
            if (onConfirm != null) {
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        }
                    ) {
                        Text("Mulai")
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Tutup")
                }
            }
        }
    )
}

@Composable
fun LanguageItemRow(
    item: UiVoskModel,
    onDownload: (VoskModelConfig) -> Unit,
    onSelect: (VoskModelConfig) -> Unit,
    onDelete: (VoskModelConfig) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.config.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.config.size,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.status == ModelStatus.DOWNLOADING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )
                Text(
                    text = if (item.progress >= 1f) "Mengekstrak..."
                        else "Mengunduh ${(item.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        when (item.status) {
            ModelStatus.ACTIVE -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Aktif",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
            }
            ModelStatus.READY -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onDelete(item.config) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Model",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = { onSelect(item.config) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Gunakan")
                    }
                }
            }
            ModelStatus.NOT_DOWNLOADED -> {
                IconButton(onClick = { onDownload(item.config) }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Unduh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            ModelStatus.DOWNLOADING -> Unit
        }
    }
}