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

    private interface SoundVoice {
        fun nextSample(): Double
        fun isFinished(): Boolean
        fun triggerFadeOut(fadeSamples: Int = 800) {}
        val isPercussiveVoice: Boolean get() = false
    }

    private val voices = CopyOnWriteArrayList<SoundVoice>()
    private val synthScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var synthJob: Job? = null
    private val lock = Any()

    // Pre-computed one-shot PCM samples for zero-latency, CC0 drum playback
    private val kickSamples by lazy { DrumAudioGenerator.generateHitSamples(com.example.model.DrumHit.KICK) }
    private val snareSamples by lazy { DrumAudioGenerator.generateHitSamples(com.example.model.DrumHit.SNARE) }
    private val closedHatSamples by lazy { DrumAudioGenerator.generateHitSamples(com.example.model.DrumHit.CLOSED_HIHAT) }
    private val openHatSamples by lazy { DrumAudioGenerator.generateHitSamples(com.example.model.DrumHit.OPEN_HIHAT) }
    private val rimClickSamples by lazy { DrumAudioGenerator.generateHitSamples(com.example.model.DrumHit.RIM_CLICK) }

    /**
     * Pre-warms the low-latency audio track and pre-loads drum PCM samples
     * so metronome drums and string taps fire instantaneously with 0 startup delay.
     */
    fun warmUp() {
        // Pre-initialize lazy drum samples in background pool
        synthScope.launch {
            kickSamples
            snareSamples
            closedHatSamples
            openHatSamples
            rimClickSamples
        }
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

    private class SampleVoice(
        val samples: ShortArray,
        val volume: Float
    ) : SoundVoice {
        var sampleIndex: Int = 0

        override val isPercussiveVoice: Boolean get() = true

        override fun isFinished(): Boolean = sampleIndex >= samples.size

        override fun nextSample(): Double {
            if (isFinished()) return 0.0
            val s = (samples[sampleIndex].toDouble() / 32768.0) * volume
            sampleIndex++
            return s
        }
    }

    /**
     * Dedicated physical-modeling string synthesizer for authentic, distortion-free chord playback.
     * Uses Karplus-Strong string synthesis with triangular pick displacement, warm acoustic lowpass
     * filtering, allpass fractional delay tuning, and calibrated linear headroom.
     */
    private class KarplusStrongChordString(
        val frequency: Double,
        val durationSamples: Int,
        val volume: Float,
        seed: Long = System.nanoTime()
    ) : SoundVoice {
        // Exact loop delay calculation accounting for the 0.5 sample FIR filter delay
        private val exactDelay = (SAMPLE_RATE / frequency).coerceIn(20.0, 3000.0)
        private val integerDelay = (exactDelay - 0.5).toInt().coerceAtLeast(10)
        private val fracDelay = ((exactDelay - 0.5) - integerDelay).coerceIn(0.0, 1.0)
        // First-order allpass coefficient for exact fractional delay tuning: C = (1 - D) / (1 + D)
        private val allpassC = if (fracDelay > 0.001) ((1.0 - fracDelay) / (1.0 + fracDelay)).toFloat() else 0f

        private val bufferLength = integerDelay
        private val buffer = FloatArray(bufferLength)
        private var readIndex = 0
        private var sampleCount = 0

        private var prevAllpassIn = 0f
        private var prevAllpassOut = 0f

        private var isFadingOut = false
        private var fadeRemaining = 0
        private var fadeStart = 0

        // Frequency-dependent acoustic decay: lower strings sustain longer (~2.6s), higher ~1.8s
        private val decayFactor: Float

        init {
            val targetSustainSec = when {
                frequency < 130.0 -> 2.6
                frequency < 220.0 -> 2.3
                frequency < 330.0 -> 2.0
                else -> 1.8
            }
            val loops = (frequency * targetSustainSec).coerceAtLeast(25.0)
            decayFactor = kotlin.math.exp(-2.9 / loops).toFloat()

            // Initialize buffer with warm acoustic pick excitation:
            // 1. Triangular string displacement matching pick location (~18% from bridge)
            // 2. Gentle filtered friction noise simulating celluloid pick release
            val random = java.util.Random(seed)
            var lpNoise = 0f
            val pickPos = 0.18

            for (i in 0 until bufferLength) {
                val whiteNoise = (random.nextFloat() * 2f - 1f)
                // 1-pole lowpass filter to remove harsh digital static above 4 kHz
                lpNoise = lpNoise * 0.42f + whiteNoise * 0.58f

                // Triangular physical displacement profile of plucked string
                val pos = i.toDouble() / bufferLength
                val displacement = if (pos < pickPos) {
                    pos / pickPos
                } else {
                    (1.0 - pos) / (1.0 - pickPos)
                }

                buffer[i] = (displacement.toFloat() * 0.55f + lpNoise * 0.45f)
            }

            // Zero-out DC offset to eliminate onset clicks or asymmetric headroom loss
            var sum = 0.0
            for (i in 0 until bufferLength) {
                sum += buffer[i]
            }
            val dcOffset = (sum / bufferLength).toFloat()
            for (i in 0 until bufferLength) {
                buffer[i] -= dcOffset
            }
        }

        override fun triggerFadeOut(fadeSamples: Int) {
            if (!isFadingOut || fadeRemaining > fadeSamples) {
                isFadingOut = true
                fadeRemaining = fadeSamples
                fadeStart = fadeSamples
            }
        }

        override fun isFinished(): Boolean {
            return sampleCount >= durationSamples || (isFadingOut && fadeRemaining <= 0)
        }

        override fun nextSample(): Double {
            if (isFinished()) return 0.0

            val current = buffer[readIndex]
            val nextIdx = if (readIndex + 1 >= bufferLength) 0 else readIndex + 1
            val next = buffer[nextIdx]

            // Two-point moving average lowpass filter (simulates string internal damping)
            val filtered = (current + next) * 0.5f * decayFactor

            // Allpass fractional delay filter to guarantee dead-accurate musical pitch
            val feedbackSample = if (allpassC > 0.001f) {
                val apOut = allpassC * filtered + prevAllpassIn - allpassC * prevAllpassOut
                prevAllpassIn = filtered
                prevAllpassOut = apOut
                apOut
            } else {
                filtered
            }

            buffer[readIndex] = feedbackSample
            readIndex = nextIdx
            sampleCount++

            var out = current.toDouble() * volume

            // Smooth 1.5ms onset envelope to eliminate edge clicks
            val attackSamples = (SAMPLE_RATE * 0.0015).toInt().coerceAtLeast(1)
            if (sampleCount < attackSamples) {
                val ratio = sampleCount.toDouble() / attackSamples
                out *= (0.5 * (1.0 - cos(PI * ratio)))
            }

            if (isFadingOut) {
                val fadeRatio = (fadeRemaining.toDouble() / fadeStart.coerceAtLeast(1)).coerceIn(0.0, 1.0)
                val fadeEnvelope = 0.5 * (1.0 - cos(PI * fadeRatio))
                fadeRemaining--
                out *= fadeEnvelope
            }

            return out
        }
    }

    private class Voice(
        val frequency: Double,
        val maxDurationSamples: Int,
        val volume: Float,
        val isPercussive: Boolean = false
    ) : SoundVoice {
        var sampleIndex: Int = 0
        var isFadingOut: Boolean = false
        var fadeOutRemaining: Int = 0
        var fadeOutStartRemaining: Int = 0

        override val isPercussiveVoice: Boolean get() = isPercussive

        override fun triggerFadeOut(fadeSamples: Int) {
            if (!isFadingOut || fadeOutRemaining > fadeSamples) {
                isFadingOut = true
                fadeOutRemaining = fadeSamples
                fadeOutStartRemaining = fadeSamples
            }
        }

        override fun isFinished(): Boolean {
            return sampleIndex >= maxDurationSamples || (isFadingOut && fadeOutRemaining <= 0)
        }

        override fun nextSample(): Double {
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

            // Smooth end-of-track release envelope:
            // Keeps the acoustic sound and harmonics 100% unaltered throughout the entire body of the note.
            // During the final ~150ms (~6,615 samples) before maxDurationSamples,
            // it smoothly and tangentially glides down to 0.0 via a raised-cosine curve,
            // eliminating the sudden step truncation that caused the end-of-track crackle and distortion.
            val tailSamples = (SAMPLE_RATE * 0.15).toInt().coerceAtLeast(100)
            val remainingSamples = maxDurationSamples - sampleIndex
            val tailEnvelope = if (remainingSamples < tailSamples) {
                val ratio = (remainingSamples.toDouble() / tailSamples).coerceIn(0.0, 1.0)
                0.5 * (1.0 - cos(PI * ratio))
            } else {
                1.0
            }

            if (isFadingOut) {
                val fadeRatio = (fadeOutRemaining.toDouble() / fadeOutStartRemaining.coerceAtLeast(1)).coerceIn(0.0, 1.0)
                val fadeEnvelope = 0.5 * (1.0 - cos(PI * fadeRatio))
                fadeOutRemaining--
                return sample * minOf(fadeEnvelope, tailEnvelope)
            }

            return sample * tailEnvelope
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
                            // Smooth transparent limiter: perfectly linear up to ±0.80, gentle tanh knee above to eliminate digital clipping
                            val limited = if (kotlin.math.abs(sum) <= 0.80) {
                                sum
                            } else {
                                val sign = if (sum > 0) 1.0 else -1.0
                                val excess = kotlin.math.abs(sum) - 0.80
                                sign * (0.80 + 0.18 * tanh(excess / 0.18))
                            }
                            chunk[i] = (limited * 32000.0).toInt().coerceIn(
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
            if (!v.isPercussiveVoice) {
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
     * Strums an entire chord with pristine acoustic physical modeling.
     * Calibrates multi-string gain to preserve clean linear headroom (eliminating distortion),
     * fires the first string immediately with zero startup delay, and strums through strings
     * at an authentic, tight 12ms pick speed.
     */
    fun strumChord(frequencies: List<Double>, volume: Float = 0.65f) {
        // Ensure low-latency audio streaming track is active immediately before coroutine dispatch
        ensureAudioLoopRunning()

        synthScope.launch {
            // Smoothly fade out previous chord strings over ~18ms (800 samples)
            // to allow rapid re-strumming without clipping or audible pops
            for (v in voices) {
                if (v is KarplusStrongChordString) {
                    v.triggerFadeOut(800)
                }
            }

            val validFreqs = frequencies.filter { it > 20.0 }
            if (validFreqs.isEmpty()) return@launch

            val numStrings = validFreqs.size
            // Calibrate individual string volumes so the combined chord waveform stays safely
            // within the linear range (below the 0.80 limiter threshold), guaranteeing zero distortion
            val perStringVolume = (volume * 0.50f / kotlin.math.sqrt(numStrings.toDouble())).toFloat()

            val durationSamples = (SAMPLE_RATE * 2.4).toInt()
            // 12ms per string simulates a natural, tight acoustic guitar down-strum
            val strumDelayMs = 12L

            for ((index, freq) in validFreqs.withIndex()) {
                val voice = KarplusStrongChordString(
                    frequency = freq,
                    durationSamples = durationSamples,
                    volume = perStringVolume,
                    seed = System.nanoTime() + index * 37L
                )
                voices.add(voice)

                // Stagger subsequent strings with tight 12ms spacing
                if (index < validFreqs.size - 1) {
                    delay(strumDelayMs)
                }
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

    /**
     * Plays a single one-shot drum hit (Kick, Snare, Closed/Open HiHat, Rim Click).
     */
    fun playDrumHit(hit: com.example.model.DrumHit, volume: Float = 0.85f) {
        val buf = when (hit) {
            com.example.model.DrumHit.KICK -> kickSamples
            com.example.model.DrumHit.SNARE -> snareSamples
            com.example.model.DrumHit.CLOSED_HIHAT -> closedHatSamples
            com.example.model.DrumHit.OPEN_HIHAT -> openHatSamples
            com.example.model.DrumHit.RIM_CLICK -> rimClickSamples
        }
        voices.add(SampleVoice(buf, volume))
        ensureAudioLoopRunning()
    }

    /**
     * Fires multiple drum hits simultaneously (e.g. Kick + Hi-Hat on beat 1).
     */
    fun playDrumHits(hits: List<com.example.model.DrumHit>, volume: Float = 0.85f) {
        if (hits.isEmpty()) return
        for (hit in hits) {
            val buf = when (hit) {
                com.example.model.DrumHit.KICK -> kickSamples
                com.example.model.DrumHit.SNARE -> snareSamples
                com.example.model.DrumHit.CLOSED_HIHAT -> closedHatSamples
                com.example.model.DrumHit.OPEN_HIHAT -> openHatSamples
                com.example.model.DrumHit.RIM_CLICK -> rimClickSamples
            }
            voices.add(SampleVoice(buf, volume))
        }
        ensureAudioLoopRunning()
    }
}
