package com.achmadichzan.rangkum.domain.repository

import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun getModelStatus(config: VoskModelConfig): ModelStatus
    fun getModelPath(config: VoskModelConfig): String?
    fun downloadModel(config: VoskModelConfig): Flow<Float>
    suspend fun deleteModel(config: VoskModelConfig)
}