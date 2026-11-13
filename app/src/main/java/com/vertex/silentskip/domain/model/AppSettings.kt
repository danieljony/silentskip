package com.vertex.silentskip.domain.model

data class AppSettings(
    val isSoundDetectionEnabled: Boolean = true,
    val isPremiumSubscriber: Boolean = false
)
