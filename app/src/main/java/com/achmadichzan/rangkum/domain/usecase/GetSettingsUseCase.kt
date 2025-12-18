package com.achmadichzan.rangkum.domain.usecase

import com.achmadichzan.rangkum.domain.repository.SettingsRepository

class GetSettingsUseCase(settingsRepository: SettingsRepository) {
    val isDarkMode = settingsRepository.isDarkMode
    val selectedModel = settingsRepository.selectedModel
    val selectedVoskModelCode = settingsRepository.selectedVoskModelCode
}