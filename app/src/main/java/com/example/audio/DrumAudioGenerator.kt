package com.example.audio

import com.example.model.DrumHit
import com.example.model.DrumPatternEngine
import com.example.model.DrumStyle
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

object DrumAudioGenerator {

    const val SAMPLE_RATE = 44100

    /**
     * Generates a 16-bit PCM mono sample array for a single one-shot drum hit.
     * Engineered for punchy, authentic acoustic drum tones with clean headroom and smooth envelopes.
     */
    fun generateHitSamples(hit: DrumHit): ShortArray {
        val durationSec = when (hit) {
            DrumHit.KICK -> 0.22
            DrumHit.SNARE -> 0.20
            DrumHit.CLOSED_HIHAT -> 0.055
            DrumHit.OPEN_HIHAT -> 0.30
            DrumHit.RIM_CLICK -> 0.050
        }
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val rng = Random(1337) // Deterministic seed for crisp, high-fidelity hits

        var phase = 0.0
        var hpFilterPrev = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sampleVal: Double = when (hit) {
                DrumHit.KICK -> {
                    // Deep, rounded acoustic kick: fast pitch drop 130Hz -> 50Hz, then settling to 45Hz
                    val freq = 45.0 + (130.0 - 45.0) * exp(-35.0 * t)
                    phase += 2.0 * PI * freq / SAMPLE_RATE
                    // Resonant fundamental + subtle warm 2nd harmonic (warmth without distortion)
                    val body = sin(phase) * exp(-14.0 * t) + 0.18 * sin(phase * 2.0) * exp(-22.0 * t)
                    // Beater click transient on the first few milliseconds (beater striking skin)
                    val beaterClick = sin(2.0 * PI * 2200.0 * t) * exp(-450.0 * t) * 0.22
                    (body * 0.78 + beaterClick) * 0.82
                }
                DrumHit.SNARE -> {
                    // Tonal snare drum body (185Hz fundamental + 330Hz harmonic shell resonance)
                    val bodyTone = (sin(2.0 * PI * 185.0 * t) * 0.65 + sin(2.0 * PI * 330.0 * t) * 0.35) * exp(-26.0 * t)
                    // Snare wire rattle: high-pass filtered noise to avoid low-mid mud & distortion
                    val rawNoise = rng.nextDouble() * 2.0 - 1.0
                    val hpNoise = rawNoise - hpFilterPrev
                    hpFilterPrev = rawNoise * 0.82
                    val snareRattle = hpNoise * exp(-20.0 * t)
                    // Crisp initial stick impact transient
                    val stickImpact = sin(2.0 * PI * 950.0 * t) * exp(-280.0 * t) * 0.30
                    (bodyTone * 0.45 + snareRattle * 0.45 + stickImpact * 0.20) * 0.78
                }
                DrumHit.CLOSED_HIHAT -> {
                    // Metallic cymbal frequencies cluster (harmonically dense, crisp shimmer)
                    val rawNoise = rng.nextDouble() * 2.0 - 1.0
                    val hpNoise = rawNoise - hpFilterPrev
                    hpFilterPrev = rawNoise * 0.88
                    // Metallic ringing overtone combo (3.2kHz, 5.4kHz, 8.1kHz)
                    val metal = (sin(2.0 * PI * 3200.0 * t) + sin(2.0 * PI * 5400.0 * t) + sin(2.0 * PI * 8100.0 * t)) * 0.25
                    val hatEnvelope = exp(-75.0 * t)
                    (hpNoise * 0.70 + metal * 0.30) * hatEnvelope * 0.60
                }
                DrumHit.OPEN_HIHAT -> {
                    // Sizzling, airy metallic wash with smooth natural decay
                    val rawNoise = rng.nextDouble() * 2.0 - 1.0
                    val hpNoise = rawNoise - hpFilterPrev
                    hpFilterPrev = rawNoise * 0.88
                    val metal = (sin(2.0 * PI * 3150.0 * t) + sin(2.0 * PI * 5600.0 * t) + sin(2.0 * PI * 7900.0 * t)) * 0.25
                    val sizzleEnv = exp(-13.0 * t)
                    (hpNoise * 0.70 + metal * 0.30) * sizzleEnv * 0.58
                }
                DrumHit.RIM_CLICK -> {
                    // Clean acoustic wood cross-stick / rim click (warm resonance at 1800Hz & 3200Hz)
                    val tone = sin(2.0 * PI * 1800.0 * t) * exp(-65.0 * t) * 0.65 +
                            sin(2.0 * PI * 3200.0 * t) * exp(-120.0 * t) * 0.35
                    tone * 0.75
                }
            }
            // Gentle linear envelope fade-out over final 64 samples to eliminate boundary click
            val fadeSamples = 64
            val fadeOut = if (i >= numSamples - fadeSamples) {
                (numSamples - 1 - i).toDouble() / fadeSamples
            } else 1.0

            // Clean headroom scaling without harsh saturation
            val sampleBounded = (sampleVal * fadeOut).coerceIn(-0.95, 0.95)
            buffer[i] = (sampleBounded * 26000.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Generates a 16-bit PCM click tick (metronome pulse).
     */
    fun generateClickSamples(isAccent: Boolean): ShortArray {
        val freq = if (isAccent) 1800.0 else 1100.0
        val durationSec = if (isAccent) 0.06 else 0.035
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-60.0 * t)
            val sampleVal = sin(2.0 * PI * freq * t) * env * 0.85
            buffer[i] = (sampleVal * 28000.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Renders a backing pattern (either Click or Drums) for a given duration into a continuous PCM ShortArray.
     */
    fun renderBackingPattern(
        isDrums: Boolean,
        drumStyle: DrumStyle,
        bpm: Int,
        timeSignature: Int,
        totalDurationMs: Long
    ): ShortArray {
        val totalSamples = ((SAMPLE_RATE.toDouble() / 1000.0) * totalDurationMs).toInt()
        val output = ShortArray(totalSamples)
        val beatIntervalMs = 60_000.0 / bpm.coerceAtLeast(30)
        val samplesPerBeat = (SAMPLE_RATE * (beatIntervalMs / 1000.0)).toInt().coerceAtLeast(1)

        // Precompute hit buffers
        val kick = generateHitSamples(DrumHit.KICK)
        val snare = generateHitSamples(DrumHit.SNARE)
        val closedHat = generateHitSamples(DrumHit.CLOSED_HIHAT)
        val openHat = generateHitSamples(DrumHit.OPEN_HIHAT)
        val rimClick = generateHitSamples(DrumHit.RIM_CLICK)
        val accentClick = generateClickSamples(true)
        val normalClick = generateClickSamples(false)

        var beatIndex = 0
        var currentSampleOffset = 0

        while (currentSampleOffset < totalSamples) {
            val beatNumber = (beatIndex % timeSignature) + 1 // 1..timeSignature
            if (!isDrums) {
                // Click mode
                val clickBuf = if (beatNumber == 1) accentClick else normalClick
                mixInto(output, clickBuf, currentSampleOffset)
            } else {
                // Drums mode
                val hits = DrumPatternEngine.getHitsForBeat(drumStyle, timeSignature, beatNumber)
                for (hit in hits) {
                    val hitBuf = when (hit) {
                        DrumHit.KICK -> kick
                        DrumHit.SNARE -> snare
                        DrumHit.CLOSED_HIHAT -> closedHat
                        DrumHit.OPEN_HIHAT -> openHat
                        DrumHit.RIM_CLICK -> rimClick
                    }
                    mixInto(output, hitBuf, currentSampleOffset)
                }
            }

            beatIndex++
            currentSampleOffset = (beatIndex * samplesPerBeat)
        }

        return output
    }

    private fun mixInto(dest: ShortArray, src: ShortArray, offset: Int) {
        val len = minOf(src.size, dest.size - offset)
        for (i in 0 until len) {
            val idx = offset + i
            if (idx in dest.indices) {
                val current = dest[idx].toDouble() / 32768.0
                val addition = src[i].toDouble() / 32768.0
                val sum = current + addition
                // Soft headroom limiter: pristine linear reproduction when within bounds, smooth compression if overlapping
                val limited = if (kotlin.math.abs(sum) < 0.85) sum else kotlin.math.tanh(sum * 0.90)
                dest[idx] = (limited * 32000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
    }

    /**
     * Saves a 16-bit Mono PCM ShortArray as a standard WAV file.
     */
    fun saveWavFile(pcmData: ShortArray, targetFile: File, sampleRate: Int = SAMPLE_RATE) {
        targetFile.parentFile?.mkdirs()
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)
        val buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray())
        buf.putInt(totalDataLen)
        buf.put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray())
        buf.putInt(16) // Subchunk1Size for PCM
        buf.putShort(1.toShort()) // AudioFormat 1 = PCM
        buf.putShort(channels.toShort())
        buf.putInt(sampleRate)
        buf.putInt(byteRate)
        buf.putShort((channels * 2).toShort()) // BlockAlign
        buf.putShort(16.toShort()) // BitsPerSample
        buf.put("data".toByteArray())
        buf.putInt(totalAudioLen)

        FileOutputStream(targetFile).use { fos ->
            fos.write(header)
            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                byteBuffer.putShort(sample)
            }
            fos.write(byteBuffer.array())
        }
    }
}
