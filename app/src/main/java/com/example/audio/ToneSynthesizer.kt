package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

class ToneSynthesizer {

    companion object {
        private const val TAG = "ToneSynthesizer"
        private const val SAMPLE_RATE = 44100
        private const val CHUNK_SIZE = 256 // ~5.8ms per chunk for ultra-low latency response
    }

    private val voices = CopyOnWriteArrayList<Voice>()
    private val synthScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var synthJob: Job? = null
    private val lock = Any()

    /**
     * Pre-warms the low-latency audio track so tapping a string responds instantaneously with 0 startup lag.
     */
    fun warmUp() {
        ensureAudioLoopRunning()
    }

    /**
     * Releases active AudioTrack and terminates synthesis coroutines.
     */
    fun release() {
        synchronized(lock) {
            synthJob?.cancel()
            synthJob = null
        }
    }

    private class Voice(
        val frequency: Double,
        val maxDurationSamples: Int,
        val volume: Float,
        val isPercussive: Boolean = false
    ) {
        var sampleIndex: Int = 0
        var isFadingOut: Boolean = false
        var fadeOutRemaining: Int = 0
        var fadeOutStartRemaining: Int = 0

        fun triggerFadeOut(fadeSamples: Int = 800) {
            if (!isFadingOut || fadeOutRemaining > fadeSamples) {
                isFadingOut = true
                fadeOutRemaining = fadeSamples
                fadeOutStartRemaining = fadeSamples
            }
        }

        fun isFinished(): Boolean {
            return sampleIndex >= maxDurationSamples || (isFadingOut && fadeOutRemaining <= 0)
        }

        fun nextSample(): Double {
            if (isFinished()) return 0.0

            val t = sampleIndex.toDouble() / SAMPLE_RATE
            val sample: Double

            if (isPercussive) {
                val env = exp(-50.0 * t)
                sample = sin(2.0 * PI * frequency * t) * env * volume * 0.8
            } else {
                val durationSec = (maxDurationSamples.toDouble() / SAMPLE_RATE).coerceAtLeast(0.2)
                val decay = 2.6 / durationSec
                val envFund = exp(-decay * t)
                val envH2 = exp(-decay * 1.7 * t)
                val envH3 = exp(-decay * 3.0 * t)
                val envH4 = exp(-decay * 4.5 * t)

                val wave = (sin(2.0 * PI * frequency * t) * envFund +
                        0.35 * sin(4.0 * PI * frequency * t) * envH2 +
                        0.15 * sin(6.0 * PI * frequency * t) * envH3 +
                        0.05 * sin(8.0 * PI * frequency * t) * envH4) / 1.55

                // Smooth ultra-fast attack envelope (3ms / ~132 samples) for zero-latency bite without onset clicks
                val attackSamples = (SAMPLE_RATE * 0.003).toInt().coerceAtLeast(1)
                val attack = if (sampleIndex < attackSamples) {
                    0.5 * (1.0 - cos(PI * sampleIndex / attackSamples))
                } else 1.0

                sample = wave * attack * volume
            }

            sampleIndex++

            if (isFadingOut) {
                val fadeRatio = (fadeOutRemaining.toDouble() / fadeOutStartRemaining.coerceAtLeast(1)).coerceIn(0.0, 1.0)
                val fadeEnvelope = 0.5 * (1.0 - cos(PI * fadeRatio))
                fadeOutRemaining--
                return sample * fadeEnvelope
            }

            return sample
        }
    }

    private fun ensureAudioLoopRunning() {
        synchronized(lock) {
            if (synthJob?.isActive == true) return

            synthJob = synthScope.launch {
                var audioTrack: AudioTrack? = null
                try {
                    val minBufSize = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    // Keep buffer tight for instantaneous speaker output
                    val bufSize = minBufSize.coerceAtLeast(CHUNK_SIZE * 4)

                    val trackBuilder = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    }

                    audioTrack = trackBuilder.build()
                    audioTrack.play()

                    val chunk = ShortArray(CHUNK_SIZE)
                    var silenceFrames = 0
                    // Keep stream warm and running for 30s of silence so user taps respond immediately with 0ms startup lag
                    val maxSilenceFrames = (SAMPLE_RATE / CHUNK_SIZE) * 30.0

                    while (isActive) {
                        if (voices.isEmpty()) {
                            silenceFrames++
                            if (silenceFrames > maxSilenceFrames) {
                                break
                            }
                            java.util.Arrays.fill(chunk, 0.toShort())
                            audioTrack.write(chunk, 0, chunk.size)
                            continue
                        }

                        silenceFrames = 0
                        val activeList = voices.toList()
                        for (i in 0 until CHUNK_SIZE) {
                            var sum = 0.0
                            for (j in 0 until activeList.size) {
                                sum += activeList[j].nextSample()
                            }
                            // Tanh soft limiter prevents any clipping even with multiple notes/taps
                            val limited = tanh(sum * 0.60)
                            chunk[i] = (limited * 25000.0).toInt().coerceIn(
                                Short.MIN_VALUE.toInt(),
                                Short.MAX_VALUE.toInt()
                            ).toShort()
                        }

                        for (j in 0 until activeList.size) {
                            val v = activeList[j]
                            if (v.isFinished()) {
                                voices.remove(v)
                            }
                        }

                        audioTrack.write(chunk, 0, chunk.size)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in ToneSynthesizer streaming loop", e)
                } finally {
                    try {
                        audioTrack?.stop()
                        audioTrack?.release()
                    } catch (_: Exception) {}
                    synchronized(lock) {
                        synthJob = null
                    }
                }
            }
        }
    }

    /**
     * Plays a rich plucked instrument string tone at the given frequency (Hz).
     * Smoothly crossfades when tapped repeatedly to eliminate clipping and pops.
     */
    fun playPluckedTone(frequency: Double, durationSec: Double = 1.6, volume: Float = 0.70f) {
        if (frequency <= 20.0) return

        // Smoothly dampen previous plucks over 18ms (800 samples) to prevent popping
        for (v in voices) {
            if (!v.isPercussive) {
                v.triggerFadeOut(800)
            }
        }

        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val voice = Voice(
            frequency = frequency,
            maxDurationSamples = numSamples,
            volume = volume.coerceIn(0.1f, 1.0f),
            isPercussive = false
        )
        voices.add(voice)
        ensureAudioLoopRunning()
    }

    /**
     * Celebratory chime when string is in tune.
     */
    fun playInTuneChime(baseFreq: Double = 587.33) {
        synthScope.launch {
            try {
                val chordNotes = listOf(baseFreq, baseFreq * 1.2599, baseFreq * 1.4983, baseFreq * 2.0)
                for (note in chordNotes) {
                    val voice = Voice(
                        frequency = note,
                        maxDurationSamples = (SAMPLE_RATE * 0.6).toInt(),
                        volume = 0.55f,
                        isPercussive = false
                    )
                    voices.add(voice)
                    ensureAudioLoopRunning()
                    delay(50)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing in-tune chime", e)
            }
        }
    }

    /**
     * Celebratory fanfare when all strings are tuned.
     */
    fun playAllStringsCompletionCelebration() {
        synthScope.launch {
            try {
                val fanfareNotes = listOf(523.25, 659.25, 783.99, 1046.50)
                for (note in fanfareNotes) {
                    val voice = Voice(
                        frequency = note,
                        maxDurationSamples = (SAMPLE_RATE * 1.0).toInt(),
                        volume = 0.65f,
                        isPercussive = false
                    )
                    voices.add(voice)
                    ensureAudioLoopRunning()
                    delay(110)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing celebration", e)
            }
        }
    }

    /**
     * Strums an entire chord (arpeggiated quickly).
     */
    fun strumChord(frequencies: List<Double>, volume: Float = 0.65f) {
        synthScope.launch {
            // Dampen prior chord voices smoothly
            for (v in voices) {
                if (!v.isPercussive) {
                    v.triggerFadeOut(1200)
                }
            }
            for (freq in frequencies) {
                if (freq > 20.0) {
                    val voice = Voice(
                        frequency = freq,
                        maxDurationSamples = (SAMPLE_RATE * 1.8).toInt(),
                        volume = volume.coerceIn(0.1f, 1.0f),
                        isPercussive = false
                    )
                    voices.add(voice)
                    ensureAudioLoopRunning()
                }
                delay(35)
            }
        }
    }

    /**
     * Plays a crisp metronome tick.
     */
    fun playMetronomeTick(isAccent: Boolean, volume: Float = 0.85f) {
        val freq = if (isAccent) 1800.0 else 1100.0
        val durationSamples = (SAMPLE_RATE * (if (isAccent) 0.08 else 0.04)).toInt()
        val voice = Voice(
            frequency = freq,
            maxDurationSamples = durationSamples,
            volume = volume,
            isPercussive = true
        )
        voices.add(voice)
        ensureAudioLoopRunning()
    }
}
