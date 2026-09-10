package com.example.model

import java.util.UUID

data class LoopTake(
    val id: String = UUID.randomUUID().toString(),
    val trackIndex: Int, // 0 = Track 1, 1 = Track 2, 2 = Track 3
    val name: String,
    val audioFilePath: String,
    val durationMs: Long,
    val recordedAtTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val waveformPoints: List<Float> = emptyList() // 24-32 normalized peak amplitudes [0.0..1.0]
)

data class LoopTrack(
    val trackIndex: Int, // 0 = Track 1, 1 = Track 2, 2 = Track 3
    val takes: List<LoopTake> = emptyList(),
    val activeTakeId: String? = null,
    val volume: Float = 1.0f, // 0.0f to 1.0f, per-track volume
    val isPlaybackEnabled: Boolean = false // Speaker / sound toggle: true when enabled in the mix
) {
    val activeTake: LoopTake?
        get() = takes.firstOrNull { it.id == activeTakeId } ?: takes.lastOrNull()
}

enum class RecordingState {
    IDLE,
    ARMED, // Arming: waiting for next Beat 1 downbeat of backing track
    RECORDING // Active microphone capture
}
