package com.vertex.silentskip.domain.model

data class AudioRecordModel(
    val id: String,
    val name: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAtFormatted: String,
    val timestamp: Long
)
