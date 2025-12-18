package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.domain.repository.ModelRepository
import kotlinx.coroutines.flow.Flow

class DownloadVoskModelUseCase(private val modelRepository: ModelRepository) {
    operator fun invoke(config: VoskModelConfig): Flow<Float> {
        return modelRepository.downloadModel(config)
    }
}