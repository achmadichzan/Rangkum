package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.ModelStatus
import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.domain.repository.ModelRepository

class GetVoskModelStatusUseCase(private val modelRepository: ModelRepository) {
    operator fun invoke(config: VoskModelConfig): ModelStatus {
        return modelRepository.getModelStatus(config)
    }
}