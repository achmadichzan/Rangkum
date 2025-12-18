package com.achmadichzan.rangkum.domain.model

data class UiVoskModel(
    val config: VoskModelConfig,
    val status: ModelStatus,
    val progress: Float = 0f
)