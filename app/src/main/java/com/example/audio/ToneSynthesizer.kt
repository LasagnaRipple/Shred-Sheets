package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class ToneSynthesizer {

    companion object {
        private const val TAG = "ToneSynthesizer"
        private const val SAMPLE_RATE = 44100
    }

    /**
     * Plays a rich plucked instrument string tone at the given frequency (Hz).
     */
    fun playPluckedTone(frequency: Double, durationSec: Double = 1.6, volume: Float = 0.8f) {
        if (frequency <= 20.0) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = (SAMPLE_RATE * durationSec).toInt()
                val samples = ShortArray(numSamples)
                val decayRate = 3.5 / durationSec // Controls natural pluck fade

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val envelope = exp(-decayRate * t)

                    // Harmonics for guitar-like timbre: Fundamental + 2nd + 3rd + 4th harmonic
                    val fundamental = sin(2.0 * PI * frequency * t)
                    val h2 = 0.5 * sin(4.0 * PI * frequency * t)
                    val h3 = 0.25 * sin(6.0 * PI * frequency * t)
                    val h4 = 0.12 * sin(8.0 * PI * frequency * t)

                    val composite = (fundamental + h2 + h3 + h4) / 1.87
                    val sampleValue = (composite * envelope * volume * Short.MAX_VALUE).toInt()
                    samples[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(samples)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing plucked tone", e)
            }
        }
    }

    /**
     * Celebratory celebratory chime when string is perfectly in tune!
     */
    fun playInTuneChime(baseFreq: Double = 587.33) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val chordNotes = listOf(baseFreq, baseFreq * 1.2599, baseFreq * 1.4983, baseFreq * 2.0)
                for (note in chordNotes) {
                    playPluckedTone(note, durationSec = 0.6, volume = 0.6f)
                    delay(50)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing in-tune chime", e)
            }
        }
    }

    /**
     * Celebratory fanfare chime when all strings on the instrument have been tuned!
     */
    fun playAllStringsCompletionCelebration() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val fanfareNotes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 triumphant arpeggio
                for (note in fanfareNotes) {
                    playPluckedTone(note, durationSec = 1.0, volume = 0.75f)
                    delay(110)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing all-strings celebration chime", e)
            }
        }
    }

    /**
     * Strums an entire chord (arpeggiated quickly).
     */
    fun strumChord(frequencies: List<Double>, volume: Float = 0.7f) {
        CoroutineScope(Dispatchers.Default).launch {
            for (freq in frequencies) {
                playPluckedTone(freq, durationSec = 1.8, volume = volume)
                delay(35) // 35ms strum stagger between strings
            }
        }
    }

    /**
     * Plays a crisp metronome tick.
     * isAccent = true for beat 1 (higher pitch ping), false for normal tick.
     */
    fun playMetronomeTick(isAccent: Boolean, volume: Float = 0.85f) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val durationSec = if (isAccent) 0.08 else 0.04
                val numSamples = (SAMPLE_RATE * durationSec).toInt()
                val samples = ShortArray(numSamples)
                val freq = if (isAccent) 1800.0 else 1100.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-45.0 * t) // fast percussive decay
                    val wave = sin(2.0 * PI * freq * t)
                    val click = (wave * env * volume * Short.MAX_VALUE).toInt()
                    samples[i] = click.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(samples)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing metronome tick", e)
            }
        }
    }

    private fun playBuffer(samples: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()

        // Clean up after playback finishes
        CoroutineScope(Dispatchers.Default).launch {
            delay((samples.size * 1000L / SAMPLE_RATE) + 200L)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
