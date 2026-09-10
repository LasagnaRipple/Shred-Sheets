package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import com.example.model.DrumStyle
import com.example.model.LoopTake
import com.example.model.LoopTrack
import com.example.model.MetronomeSoundMode
import com.example.model.RecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tanh

class LoopStationEngine(private val context: Context) {

    companion object {
        private const val TAG = "LoopStationEngine"
        const val SAMPLE_RATE = 44100
        const val MAX_TAKES_PER_TRACK = 50
        const val FIXED_LOOP_BARS = 4

        fun getLoopDurationSeconds(bpm: Int = 100, timeSignature: Int = 4): Float {
            val secondsPerBar = (60.0f / bpm.coerceAtLeast(30)) * timeSignature.coerceAtLeast(1)
            return FIXED_LOOP_BARS * secondsPerBar
        }
    }

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Tracks 1, 2
    private val _tracks = MutableStateFlow(
        listOf(
            LoopTrack(trackIndex = 0),
            LoopTrack(trackIndex = 1)
        )
    )
    val tracks: StateFlow<List<LoopTrack>> = _tracks.asStateFlow()

    // Recording state
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _armedTrackIndex = MutableStateFlow<Int?>(null)
    val armedTrackIndex: StateFlow<Int?> = _armedTrackIndex.asStateFlow()

    private val _recordingTrackIndex = MutableStateFlow<Int?>(null)
    val recordingTrackIndex: StateFlow<Int?> = _recordingTrackIndex.asStateFlow()

    private val _recordingElapsedMs = MutableStateFlow(0L)
    val recordingElapsedMs: StateFlow<Long> = _recordingElapsedMs.asStateFlow()

    private val _liveInputLevel = MutableStateFlow(0f)
    val liveInputLevel: StateFlow<Float> = _liveInputLevel.asStateFlow()

    // Loop cycle progress [0.0f..1.0f] and current bar in loop
    private val _loopProgress = MutableStateFlow(0f)
    val loopProgress: StateFlow<Float> = _loopProgress.asStateFlow()

    private val _currentBarIndex = MutableStateFlow(0)
    val currentBarIndex: StateFlow<Int> = _currentBarIndex.asStateFlow()

    // Backing volume [0.0f..1.0f]
    private val _backingVolume = MutableStateFlow(0.85f)
    val backingVolume: StateFlow<Float> = _backingVolume.asStateFlow()

    // Loop length in seconds (restricted strictly to 4 bars, dynamically synced with BPM and time signature)
    private val _loopLengthSeconds = MutableStateFlow(getLoopDurationSeconds(100, 4))
    val loopLengthSeconds: StateFlow<Float> = _loopLengthSeconds.asStateFlow()

    // Headphone tip dismissed state
    private val _showHeadphoneTip = MutableStateFlow(true)
    val showHeadphoneTip: StateFlow<Boolean> = _showHeadphoneTip.asStateFlow()

    // Previewing take state
    private val _previewingTakeId = MutableStateFlow<String?>(null)
    val previewingTakeId: StateFlow<String?> = _previewingTakeId.asStateFlow()

    // Currently recording take number (1-based)
    private val _currentRecordingTakeNumber = MutableStateFlow(1)
    val currentRecordingTakeNumber: StateFlow<Int> = _currentRecordingTakeNumber.asStateFlow()

    private var activePlaybackJob: Job? = null
    private var recordingJob: Job? = null
    private var previewJob: Job? = null

    fun dismissHeadphoneTip() {
        _showHeadphoneTip.value = false
    }

    fun setBackingVolume(vol: Float) {
        _backingVolume.value = vol.coerceIn(0f, 1f)
    }

    fun setTrackVolume(trackIndex: Int, vol: Float) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            list[trackIndex] = list[trackIndex].copy(volume = vol.coerceIn(0f, 1f))
            _tracks.value = list
        }
    }

    fun hasAnyTakes(): Boolean {
        return _tracks.value.any { it.takes.isNotEmpty() }
    }

    fun clearAllTakes() {
        _tracks.value = _tracks.value.map { it.copy(takes = emptyList(), activeTakeId = null) }
    }

    fun syncLoopDuration(bpm: Int, timeSignature: Int) {
        _loopLengthSeconds.value = getLoopDurationSeconds(bpm, timeSignature)
    }

    fun setLoopLengthSeconds(seconds: Float) {
        _loopLengthSeconds.value = seconds.coerceIn(2f, 32f)
    }

    /**
     * Calculates the valid whole-bar snapped seconds given current BPM and time signature.
     * Enforces the 4-bar restriction.
     */
    fun calculateSnappedDuration(rawSeconds: Float, bpm: Int, timeSignature: Int): Float {
        return getLoopDurationSeconds(bpm, timeSignature)
    }

    fun calculateTotalBars(bpm: Int = 100, timeSignature: Int = 4): Int {
        return FIXED_LOOP_BARS
    }

    /**
     * User taps a track record pad:
     * - If track is already recording: stops and finishes take.
     * - If track is armed: cancels arming.
     * - Otherwise: arms track to begin on the next Beat 1 downbeat.
     */
    fun onTrackPadClicked(
        trackIndex: Int,
        bpm: Int,
        timeSignature: Int,
        isBackingPlaying: Boolean,
        onEnsureBackingStarted: () -> Unit
    ) {
        if (_recordingState.value == RecordingState.RECORDING) {
            if (_recordingTrackIndex.value == trackIndex) {
                // Manual early stop
                stopRecording()
            }
            return
        }

        if (_recordingState.value == RecordingState.ARMED && _armedTrackIndex.value == trackIndex) {
            // Disarm
            _recordingState.value = RecordingState.IDLE
            _armedTrackIndex.value = null
            return
        }

        // Arm track
        _armedTrackIndex.value = trackIndex
        _recordingState.value = RecordingState.ARMED

        if (!isBackingPlaying) {
            onEnsureBackingStarted()
        }
    }

    /**
     * Called by the Metronome beat pulse engine whenever a beat fires.
     * If a track is ARMED and current beat == 1, recording begins immediately!
     */
    /**
     * Called by the Metronome beat pulse engine whenever a beat fires.
     * When a track is ARMED, recording begins ONLY when the drum/click track is at the
     * very start of the 4-bar loop (isLoopStart == true).
     */
    fun onBackingBeatFired(
        beatNumber: Int,
        barIndex: Int,
        isLoopStart: Boolean,
        bpm: Int,
        timeSignature: Int
    ) {
        val loopDurationSec = getLoopDurationSeconds(bpm, timeSignature)
        _loopLengthSeconds.value = loopDurationSec

        if (_recordingState.value == RecordingState.ARMED && isLoopStart) {
            val armedIndex = _armedTrackIndex.value
            if (armedIndex != null) {
                _armedTrackIndex.value = null
                _recordingTrackIndex.value = armedIndex
                _recordingState.value = RecordingState.RECORDING
                startAudioRecordCapture(armedIndex, loopDurationSec)
            }
        }
    }

    /**
     * Updates the continuous loop progress and bar position.
     */
    fun updateLoopPosition(elapsedInCycleSec: Float, bpm: Int, timeSignature: Int) {
        val loopLen = getLoopDurationSeconds(bpm, timeSignature).coerceAtLeast(0.5f)
        val progress = (elapsedInCycleSec % loopLen) / loopLen
        _loopProgress.value = progress.coerceIn(0f, 1f)

        val secondsPerBar = (60.0f / bpm.coerceAtLeast(30)) * timeSignature.coerceAtLeast(1)
        val barIndex = (elapsedInCycleSec / secondsPerBar).toInt()
        _currentBarIndex.value = barIndex % FIXED_LOOP_BARS
    }

    fun stopRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            _recordingState.value = RecordingState.IDLE
            recordingJob?.cancel()
            recordingJob = null
        } else if (_recordingState.value == RecordingState.ARMED) {
            _recordingState.value = RecordingState.IDLE
            _armedTrackIndex.value = null
        }
    }

    private fun finishRecordingTake(trackIndex: Int) {
        stopRecording()
    }

    private fun startAudioRecordCapture(trackIndex: Int, targetDurationSec: Float) {
        recordingJob?.cancel()
        recordingJob = engineScope.launch {
            val targetDurationMs = (targetDurationSec * 1000L).toLong()
            val targetSamplesPerTake = (targetDurationSec * SAMPLE_RATE).roundToInt()
            _recordingElapsedMs.value = 0L

            var audioRecord: AudioRecord? = null
            val currentTakePcmList = ArrayList<Short>(targetSamplesPerTake)
            var currentTakeNumber = (_tracks.value.getOrNull(trackIndex)?.takes?.size ?: 0) + 1
            _currentRecordingTakeNumber.value = currentTakeNumber

            try {
                val minBuf = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = max(minBuf, 2048)

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord could not be initialized")
                    _recordingState.value = RecordingState.IDLE
                    _recordingTrackIndex.value = null
                    return@launch
                }

                audioRecord.startRecording()
                val chunk = ShortArray(1024)
                var takeStartTime = System.currentTimeMillis()

                // Continue recording takes back-to-back until the user stops recording
                while (isActive && _recordingState.value == RecordingState.RECORDING) {
                    val read = audioRecord.read(chunk, 0, chunk.size)
                    if (read > 0) {
                        var sumSq = 0.0
                        for (i in 0 until read) {
                            currentTakePcmList.add(chunk[i])
                            val norm = chunk[i].toDouble() / 32768.0
                            sumSq += norm * norm
                        }
                        val rms = sqrt(sumSq / read)
                        _liveInputLevel.value = (rms * 4.0).toFloat().coerceIn(0f, 1f)
                    }

                    val elapsed = System.currentTimeMillis() - takeStartTime
                    _recordingElapsedMs.value = elapsed

                    // Check if current take has completed a full loop cycle
                    if (currentTakePcmList.size >= targetSamplesPerTake) {
                        val completedTakePcm = ShortArray(targetSamplesPerTake)
                        for (i in 0 until targetSamplesPerTake) {
                            completedTakePcm[i] = currentTakePcmList[i]
                        }

                        // Carry over any excess samples seamlessly into next take
                        val leftoverCount = currentTakePcmList.size - targetSamplesPerTake
                        val nextTakeList = ArrayList<Short>(targetSamplesPerTake)
                        for (i in 0 until leftoverCount) {
                            nextTakeList.add(currentTakePcmList[targetSamplesPerTake + i])
                        }
                        currentTakePcmList.clear()
                        currentTakePcmList.addAll(nextTakeList)

                        // Save this completed take
                        saveTakeFromPcm(trackIndex, completedTakePcm, currentTakeNumber)

                        // Seamlessly transition to the next take!
                        currentTakeNumber++
                        _currentRecordingTakeNumber.value = currentTakeNumber
                        takeStartTime = System.currentTimeMillis()
                        _recordingElapsedMs.value = (currentTakePcmList.size.toDouble() / SAMPLE_RATE * 1000.0).toLong()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing audio in LoopStationEngine", e)
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}

                _liveInputLevel.value = 0f
                _recordingState.value = RecordingState.IDLE
                _recordingTrackIndex.value = null

                // If stopped mid-take and have recorded at least 1.0s (or 500ms if initial take)
                val minSamples = if ((_tracks.value.getOrNull(trackIndex)?.takes?.isEmpty() == true) && currentTakeNumber == 1) {
                    SAMPLE_RATE / 2
                } else {
                    SAMPLE_RATE
                }

                if (currentTakePcmList.size >= minSamples) {
                    saveTakeFromPcm(trackIndex, currentTakePcmList.toShortArray(), currentTakeNumber)
                }
            }
        }
    }

    private fun saveTakeFromPcm(trackIndex: Int, pcmData: ShortArray, takeNumber: Int) {
        engineScope.launch {
            try {
                val takesDir = File(context.filesDir, "loop_takes").apply { mkdirs() }
                val takeId = UUID.randomUUID().toString()
                val takeFile = File(takesDir, "take_${trackIndex}_${takeId}.wav")

                DrumAudioGenerator.saveWavFile(pcmData, takeFile, SAMPLE_RATE)

                // Compute 28 waveform thumbnail points
                val waveform = computeWaveformThumbnail(pcmData, 28)
                val durationMs = (pcmData.size.toDouble() / SAMPLE_RATE * 1000.0).toLong()

                synchronized(this@LoopStationEngine) {
                    val currentTrackList = _tracks.value.toMutableList()
                    if (trackIndex in currentTrackList.indices) {
                        val track = currentTrackList[trackIndex]
                        val newTake = LoopTake(
                            id = takeId,
                            trackIndex = trackIndex,
                            name = "Take $takeNumber",
                            audioFilePath = takeFile.absolutePath,
                            durationMs = durationMs,
                            waveformPoints = waveform
                        )

                        // Cap to MAX_TAKES_PER_TRACK. New take is selected as active, but playback is not auto-enabled
                        val updatedTakes = (track.takes + newTake).takeLast(MAX_TAKES_PER_TRACK)
                        currentTrackList[trackIndex] = track.copy(
                            takes = updatedTakes,
                            activeTakeId = newTake.id,
                            isPlaybackEnabled = false
                        )
                        _tracks.value = currentTrackList
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving take", e)
            }
        }
    }

    private fun computeWaveformThumbnail(samples: ShortArray, targetPoints: Int): List<Float> {
        if (samples.isEmpty()) return emptyList()
        val points = mutableListOf<Float>()
        val step = samples.size / targetPoints.coerceAtLeast(1)
        if (step <= 0) return List(targetPoints) { 0.1f }

        for (i in 0 until targetPoints) {
            val start = i * step
            val end = min(start + step, samples.size)
            var maxVal = 0
            for (j in start until end) {
                val a = abs(samples[j].toInt())
                if (a > maxVal) maxVal = a
            }
            val norm = (maxVal.toFloat() / 32768.0f).coerceIn(0.08f, 1.0f)
            points.add(norm)
        }
        return points
    }

    fun toggleTrackPlayback(trackIndex: Int) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            val current = list[trackIndex]
            list[trackIndex] = current.copy(isPlaybackEnabled = !current.isPlaybackEnabled)
            _tracks.value = list
        }
    }

    fun setTrackPlayback(trackIndex: Int, enabled: Boolean) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            val current = list[trackIndex]
            list[trackIndex] = current.copy(isPlaybackEnabled = enabled)
            _tracks.value = list
        }
    }

    fun cycleTake(trackIndex: Int, direction: Int) {
        val list = _tracks.value.toMutableList()
        if (trackIndex !in list.indices) return
        val track = list[trackIndex]
        if (track.takes.isEmpty()) return

        val currentIndex = track.takes.indexOfFirst { it.id == track.activeTakeId }.let { if (it < 0) 0 else it }
        val newIndex = (currentIndex + direction + track.takes.size) % track.takes.size
        list[trackIndex] = track.copy(activeTakeId = track.takes[newIndex].id)
        _tracks.value = list
    }

    fun setActiveTake(trackIndex: Int, takeId: String) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            list[trackIndex] = list[trackIndex].copy(activeTakeId = takeId)
            _tracks.value = list
        }
    }

    fun updateTakeName(trackIndex: Int, takeId: String, newName: String) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            val track = list[trackIndex]
            val updatedTakes = track.takes.map {
                if (it.id == takeId) it.copy(name = newName.ifBlank { it.name }) else it
            }
            list[trackIndex] = track.copy(takes = updatedTakes)
            _tracks.value = list
        }
    }

    fun updateTakeNotes(trackIndex: Int, takeId: String, notes: String) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            val track = list[trackIndex]
            val updatedTakes = track.takes.map {
                if (it.id == takeId) it.copy(notes = notes) else it
            }
            list[trackIndex] = track.copy(takes = updatedTakes)
            _tracks.value = list
        }
    }

    fun deleteTake(trackIndex: Int, takeId: String) {
        val list = _tracks.value.toMutableList()
        if (trackIndex in list.indices) {
            val track = list[trackIndex]
            val takeToDelete = track.takes.firstOrNull { it.id == takeId }
            takeToDelete?.let { File(it.audioFilePath).delete() }

            val updatedTakes = track.takes.filter { it.id != takeId }
            val newActiveId = if (track.activeTakeId == takeId) {
                updatedTakes.lastOrNull()?.id
            } else {
                track.activeTakeId
            }
            list[trackIndex] = track.copy(takes = updatedTakes, activeTakeId = newActiveId)
            _tracks.value = list
        }
    }

    fun previewTake(take: LoopTake) {
        if (_previewingTakeId.value == take.id) {
            stopPreview()
            return
        }
        stopPreview()
        _previewingTakeId.value = take.id

        previewJob = engineScope.launch {
            var track: AudioTrack? = null
            try {
                val file = File(take.audioFilePath)
                if (!file.exists()) return@launch
                val pcm = readWavPcm(file)

                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(max(minBuf, pcm.size * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                track.write(pcm, 0, pcm.size)
                delay(take.durationMs)
            } catch (e: Exception) {
                Log.e(TAG, "Error previewing take", e)
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
                _previewingTakeId.value = null
            }
        }
    }

    fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        _previewingTakeId.value = null
    }

    /**
     * Reads PCM ShortArray from a 44-byte header WAV file.
     */
    fun readWavPcm(file: File): ShortArray {
        val bytes = file.readBytes()
        if (bytes.size <= 44) return ShortArray(0)
        val pcmBytesLen = bytes.size - 44
        val shorts = ShortArray(pcmBytesLen / 2)
        val buf = ByteBuffer.wrap(bytes, 44, pcmBytesLen).order(ByteOrder.LITTLE_ENDIAN)
        for (i in shorts.indices) {
            shorts[i] = buf.short
        }
        return shorts
    }

    /**
     * Bounces the entire mix (Backing + Track 1 + Track 2) into an exportable WAV file.
     * Respects each track's volume slider and backing volume slider.
     */
    suspend fun bounceMix(
        isDrums: Boolean,
        drumStyle: DrumStyle,
        bpm: Int,
        timeSignature: Int
    ): File {
        val durationMs = (_loopLengthSeconds.value * 1000L).toLong()
        val totalSamples = ((SAMPLE_RATE.toDouble() / 1000.0) * durationMs).toInt()

        // 1. Render backing track
        val backingPcm = DrumAudioGenerator.renderBackingPattern(
            isDrums = isDrums,
            drumStyle = drumStyle,
            bpm = bpm,
            timeSignature = timeSignature,
            totalDurationMs = durationMs
        )

        // 2. Read PCM for each active take with playback enabled
        val tracksList = _tracks.value
        val trackPcms = tracksList.map { track ->
            val active = track.activeTake
            if (active != null && track.isPlaybackEnabled) {
                val f = File(active.audioFilePath)
                if (f.exists()) readWavPcm(f) else null
            } else null
        }

        // 3. Mix all 4 channels with volumes & soft limiter
        val mixed = ShortArray(totalSamples)
        val bVol = _backingVolume.value

        for (i in 0 until totalSamples) {
            var sum = 0.0

            // Backing
            if (i in backingPcm.indices) {
                sum += (backingPcm[i].toDouble() / 32768.0) * bVol
            }

            // Tracks 1, 2, 3
            for (t in 0..2) {
                val pcm = trackPcms.getOrNull(t)
                val vol = tracksList.getOrNull(t)?.volume ?: 1.0f
                if (pcm != null && i in pcm.indices) {
                    sum += (pcm[i].toDouble() / 32768.0) * vol
                }
            }

            // Tanh soft limiter avoids clipping
            val limited = tanh(sum * 0.90)
            mixed[i] = (limited * 31500.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        // 4. Save to export file
        val exportDir = File(context.cacheDir, "exported_mixes").apply { mkdirs() }
        val exportFile = File(exportDir, "ShredSheets_Mix_${System.currentTimeMillis()}.wav")
        DrumAudioGenerator.saveWavFile(mixed, exportFile, SAMPLE_RATE)
        return exportFile
    }

    /**
     * Mixes active takes in real-time during looping playback.
     */
    fun startRealtimeLoopMixer() {
        if (activePlaybackJob?.isActive == true) return
        activePlaybackJob = engineScope.launch {
            var track: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val chunkSize = 512
                val bufSize = max(minBuf, chunkSize * 4)

                track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(bufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                val chunk = ShortArray(chunkSize)

                var sampleCursor = 0

                while (isActive) {
                    val loopLenSec = _loopLengthSeconds.value
                    val totalCycleSamples = (SAMPLE_RATE * loopLenSec).toInt().coerceAtLeast(chunkSize)

                    val currentTrackList = _tracks.value
                    val activePcms = currentTrackList.map { t ->
                        val take = t.activeTake
                        if (take != null && t.isPlaybackEnabled) {
                            val f = File(take.audioFilePath)
                            if (f.exists()) Pair(readWavPcm(f), t.volume) else null
                        } else null
                    }

                    val hasAnyActiveTake = activePcms.any { it != null }
                    if (!hasAnyActiveTake) {
                        delay(20)
                        sampleCursor = (sampleCursor + (SAMPLE_RATE * 0.02).toInt()) % totalCycleSamples
                        continue
                    }

                    for (i in 0 until chunkSize) {
                        val pos = (sampleCursor + i) % totalCycleSamples
                        var sum = 0.0
                        for (p in activePcms) {
                            if (p != null) {
                                val (pcm, vol) = p
                                if (pos in pcm.indices) {
                                    sum += (pcm[pos].toDouble() / 32768.0) * vol
                                }
                            }
                        }
                        val limited = tanh(sum * 0.85)
                        chunk[i] = (limited * 30000.0).toInt().toShort()
                    }

                    sampleCursor = (sampleCursor + chunkSize) % totalCycleSamples
                    track.write(chunk, 0, chunkSize)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in realtime loop mixer", e)
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
                activePlaybackJob = null
            }
        }
    }

    fun stopRealtimeLoopMixer() {
        activePlaybackJob?.cancel()
        activePlaybackJob = null
    }

    fun release() {
        stopRealtimeLoopMixer()
        stopPreview()
        recordingJob?.cancel()
    }
}
