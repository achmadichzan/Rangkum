package com.achmadichzan.rangkum.data.repository

import android.content.Context
import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.domain.repository.ModelRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class ModelRepositoryImpl(
    context: Context,
    private val client: HttpClient,
    private val ioDispatcher: CoroutineDispatcher
) : ModelRepository {
    private val modelsDir = File(context.filesDir, "vosk-models")

    override suspend fun getModelStatus(config: VoskModelConfig): ModelStatus {
        return withContext(ioDispatcher) {
            val modelFile = File(modelsDir, config.folderName)
            if (modelFile.exists() && modelFile.isDirectory) {
                ModelStatus.READY
            } else {
                ModelStatus.NOT_DOWNLOADED
            }
        }
    }

    override suspend fun getModelPath(config: VoskModelConfig): String? {
        return withContext(ioDispatcher) {
            val file = File(modelsDir, config.folderName)
            if (file.exists()) file.absolutePath else null
        }
    }

    override fun downloadModel(config: VoskModelConfig): Flow<Float> = flow {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }

        val partFile = File(modelsDir, "${config.code}.zip.part")
        val finalFile = File(modelsDir, "${config.code}.zip")

        if (finalFile.exists()) {
            emit(2f)
            return@flow
        }

        try {
            emit(0f)

            val downloadedBytes = if (partFile.exists()) partFile.length() else 0L

            client.prepareGet(config.url) {
                if (downloadedBytes > 0) {
                    header("Range", "bytes=$downloadedBytes-")
                }
            }.execute { httpResponse ->

                val isResuming = httpResponse.status == HttpStatusCode.PartialContent

                val contentLength = httpResponse.contentLength() ?: 0L
                val totalBytes = if (isResuming) contentLength + downloadedBytes else contentLength

                val fileStream = FileOutputStream(partFile, isResuming)

                val channel = httpResponse.bodyAsChannel()
                var bytesCopied: Long = 0
                val buffer = ByteArray(16 * 1024)

                if (isResuming) {
                    emit(downloadedBytes.toFloat() / totalBytes.toFloat())
                }

                while (!channel.isClosedForRead) {
                    val bytes = channel.readAvailable(buffer, 0, buffer.size)
                    if (bytes == -1) break

                    fileStream.write(buffer, 0, bytes)
                    bytesCopied += bytes

                    val currentTotal = if (isResuming) downloadedBytes + bytesCopied else bytesCopied
                    emit(currentTotal.toFloat() / totalBytes.toFloat())
                }

                fileStream.flush()
                fileStream.close()
            }

            emit(1.1f)

            if (partFile.renameTo(finalFile)) {
                unzip(finalFile, modelsDir)
                finalFile.delete()
                emit(2f)
            } else {
                throw Exception("Gagal rename file .part")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }.flowOn(ioDispatcher)

    override suspend fun deleteModel(config: VoskModelConfig) {
        withContext(ioDispatcher) {
            val file = File(modelsDir, config.folderName)
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
    }

    private fun unzip(zipFile: File, targetDir: File) {
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                if (!file.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw SecurityException("Zip Path Traversal Detected")
                }
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { output ->
                        zip.copyTo(output)
                    }
                }
                entry = zip.nextEntry
            }
        }
    }
}