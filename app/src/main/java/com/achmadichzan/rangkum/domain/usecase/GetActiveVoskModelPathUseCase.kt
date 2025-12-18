package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.model.AVAILABLE_MODELS
import com.achmadichzan.rangkum.domain.repository.ModelRepository
import com.achmadichzan.rangkum.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GetActiveVoskModelPathUseCase(
    private val settingsRepository: SettingsRepository,
    private val modelRepository: ModelRepository
) {
    operator fun invoke(): Flow<String?> {
        return settingsRepository.selectedVoskModelCode
            .map { code ->
                if (code == null) return@map null

                val config = AVAILABLE_MODELS.find { it.code == code }
                if (config == null) return@map null

                modelRepository.getModelPath(config)
            }
            .distinctUntilChanged()
    }
}