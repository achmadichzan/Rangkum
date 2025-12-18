package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.VoskModelConfig
import com.achmadichzan.rangkum.domain.repository.ModelRepository

class DeleteVoskModelUseCase(private val modelRepository: ModelRepository) {
    suspend operator fun invoke(config: VoskModelConfig) {
        modelRepository.deleteModel(config)
    }
}