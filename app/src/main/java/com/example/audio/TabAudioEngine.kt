package com.example.audio

import android.content.Context
import android.util.Log
import com.example.model.GuitarTabMapper
import com.example.model.TabGridResolution
import com.example.model.TabNote
import com.example.model.TabStringDefaults
import com.example.model.TabStringDef
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
import kotlin.math.roundToInt

/**
 * Audio Engine for Tab Studio:
 * Handles real-time audio pitch transcription, note quantisation,
 * synchronized click-track playback, and interactive note editing.
 */
class TabAudioEngine(
    private val context: Context,
    private val toneSynthesizer: ToneSynthesizer
) {
    companion object {
        private const val TAG = "TabAudioEngine"
        const val DEFAULT_BPM = 112
        const val DEFAULT_BARS = 4
    }

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Riff State
    private val _riffTitle = MutableStateFlow("New Tab")
    val riffTitle: StateFlow<String> = _riffTitle.asStateFlow()

    private val _bpm = MutableStateFlow(DEFAULT_BPM)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _resolution = MutableStateFlow(TabGridResolution.EIGHTH)
    val resolution: StateFlow<TabGridResolution> = _resolution.asStateFlow()

    private val _totalBars = MutableStateFlow(DEFAULT_BARS)
    val totalBars: StateFlow<Int> = _totalBars.asStateFlow()

    // Notes on the tab staff (starts clean and empty)
    private val _notes = MutableStateFlow<List<TabNote>>(emptyList())
    val notes: StateFlow<List<TabNote>> = _notes.asStateFlow()

    private val _selectedNoteId = MutableStateFlow<String?>(null)
    val selectedNoteId: StateFlow<String?> = _selectedNoteId.asStateFlow()

    // Multi-select mode state (Set of note IDs selected for bulk actions like bulk delete)
    private val _multiSelectedNoteIds = MutableStateFlow<Set<String>>(emptySet())
    val multiSelectedNoteIds: StateFlow<Set<String>> = _multiSelectedNoteIds.asStateFlow()

    // Editor bottom tray visibility and active slot tracking (String D fret 0 by default)
    private val _isEditorTrayOpen = MutableStateFlow(false)
    val isEditorTrayOpen: StateFlow<Boolean> = _isEditorTrayOpen.asStateFlow()

    private val _activeStringIndex = MutableStateFlow(3) // 3 is D string
    val activeStringIndex: StateFlow<Int> = _activeStringIndex.asStateFlow()

    private val _activeStepIndex = MutableStateFlow(0)
    val activeStepIndex: StateFlow<Int> = _activeStepIndex.asStateFlow()

    private val _activeFret = MutableStateFlow(0)
    val activeFret: StateFlow<Int> = _activeFret.asStateFlow()

    // Undo stack for user edits
    private val undoStack = mutableListOf<List<TabNote>>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    fun pushUndoState() {
        undoStack.add(_notes.value.toList())
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        _canUndo.value = true
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previousNotes = undoStack.removeAt(undoStack.lastIndex)
            _notes.value = previousNotes
            _multiSelectedNoteIds.value = emptySet()
            val noteAtActiveSlot = previousNotes.find {
                it.stringIndex == _activeStringIndex.value && it.stepIndex == _activeStepIndex.value
            }
            if (noteAtActiveSlot != null) {
                _selectedNoteId.value = noteAtActiveSlot.id
                _activeFret.value = noteAtActiveSlot.fret
            } else {
                _selectedNoteId.value = null
            }
            _canUndo.value = undoStack.isNotEmpty()
        }
    }

    // Playback & Recording Engine States
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isCountingIn = MutableStateFlow(false)
    val isCountingIn: StateFlow<Boolean> = _isCountingIn.asStateFlow()

    private val _countInBeat = MutableStateFlow(1)
    val countInBeat: StateFlow<Int> = _countInBeat.asStateFlow()

    private val _currentPlaybackStep = MutableStateFlow(-1)
    val currentPlaybackStep: StateFlow<Int> = _currentPlaybackStep.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _isClickEnabled = MutableStateFlow(true)
    val isClickEnabled: StateFlow<Boolean> = _isClickEnabled.asStateFlow()

    private val _isCountInEnabled = MutableStateFlow(true)
    val isCountInEnabled: StateFlow<Boolean> = _isCountInEnabled.asStateFlow()

    // Live Transcription Feedback
    private val _liveDetectedNote = MutableStateFlow<String?>(null)
    val liveDetectedNote: StateFlow<String?> = _liveDetectedNote.asStateFlow()

    private val _liveDetectedPluck = MutableStateFlow<Pair<Int, Int>?>(null) // (stringIndex, fret)
    val liveDetectedPluck: StateFlow<Pair<Int, Int>?> = _liveDetectedPluck.asStateFlow()

    // Pitch detector for real-time transcription
    private val pitchDetector = PitchDetector()

    // Coroutine Jobs
    private var playbackJob: Job? = null
    private var recordingJob: Job? = null

    // Track active instrument strings (default 6-string guitar)
    val strings: List<TabStringDef> = TabStringDefaults.GUITAR_STRINGS

    val totalSteps: Int
        get() = _totalBars.value * _resolution.value.stepsPerBar

    // ─────────────────────────────────────────────────────────────────────────────
    // Playback Loop
    // ─────────────────────────────────────────────────────────────────────────────

    fun togglePlayback() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback(fromStep: Int = 0) {
        if (_isRecording.value) {
            stopRecording()
        }
        stopPlayback()
        toneSynthesizer.warmUp()

        _isPlaying.value = true
        _currentPlaybackStep.value = fromStep.coerceIn(0, totalSteps - 1)

        playbackJob = engineScope.launch {
            val stepDurMs = ((60_000.0 / _bpm.value) / _resolution.value.stepsPerBeat).toLong()
            var step = _currentPlaybackStep.value

            while (isActive && _isPlaying.value) {
                _currentPlaybackStep.value = step

                // 1. Play metronome tick if click track is enabled
                if (_isClickEnabled.value && (step % _resolution.value.stepsPerBeat == 0)) {
                    val isAccent = (step % _resolution.value.stepsPerBar == 0)
                    toneSynthesizer.playMetronomeTick(isAccent = isAccent, volume = 0.75f)
                }

                // 2. Play all tab notes scheduled at this step
                val currentNotes = _notes.value.filter { it.stepIndex == step }
                for (note in currentNotes) {
                    val freq = note.getFrequency(strings)
                    toneSynthesizer.playPluckedTone(freq, durationSec = 0.85, volume = 0.80f)
                }

                delay(stepDurMs)

                // Advance step
                step++
                if (step >= totalSteps) {
                    if (_isLooping.value) {
                        step = 0
                    } else {
                        _currentPlaybackStep.value = -1
                        _isPlaying.value = false
                        break
                    }
                }
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _isPlaying.value = false
        _currentPlaybackStep.value = -1
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Real-Time Audio Transcription & Recording
    // ─────────────────────────────────────────────────────────────────────────────

    fun toggleRecording() {
        if (_isRecording.value || _isCountingIn.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    fun startRecording() {
        stopPlayback()
        stopRecording()
        toneSynthesizer.warmUp()

        recordingJob = engineScope.launch {
            val beatDurMs = (60_000.0 / _bpm.value).toLong()

            // 1. Count-In phase (1 bar = 4 beats) if enabled
            if (_isCountInEnabled.value) {
                _isCountingIn.value = true
                for (beat in 1..4) {
                    _countInBeat.value = beat
                    val isAccent = (beat == 1)
                    toneSynthesizer.playMetronomeTick(isAccent = isAccent, volume = 0.95f)
                    delay(beatDurMs)
                }
                _isCountingIn.value = false
            }

            // 2. Recording & Metronome loop
            _isRecording.value = true
            _currentPlaybackStep.value = 0
            val stepDurMs = ((60_000.0 / _bpm.value) / _resolution.value.stepsPerBeat).toLong()
            val startTimeMs = System.currentTimeMillis()

            // Start pitch detector
            pitchDetector.startListening(engineScope)

            // Audio transcription listener
            var lastTriggeredStep = -1
            var lastTriggeredString = -1

            val pitchCollectorJob = launch {
                pitchDetector.pitchState.collect { pitchResult ->
                    if (!_isRecording.value) return@collect

                    if (pitchResult.confidence >= 0.35 && pitchResult.amplitude >= 0.0012 && pitchResult.frequency >= 70.0) {
                        val match = GuitarTabMapper.mapFrequencyToGuitarFret(pitchResult.frequency, strings)
                        if (match != null) {
                            val (stringIndex, fret) = match
                            val currentElapsed = System.currentTimeMillis() - startTimeMs
                            val rawStep = (currentElapsed.toDouble() / stepDurMs).roundToInt()
                            val quantizedStep = rawStep % totalSteps

                            // Slot note with debounce to prevent duplicate jitter on single pluck
                            if (quantizedStep != lastTriggeredStep || stringIndex != lastTriggeredString) {
                                lastTriggeredStep = quantizedStep
                                lastTriggeredString = stringIndex

                                val stringDef = strings.getOrNull(stringIndex)
                                _liveDetectedNote.value = "${stringDef?.label ?: "Str $stringIndex"}: Fret $fret (${pitchResult.noteName})"
                                _liveDetectedPluck.value = Pair(stringIndex, fret)

                                // Add or update note at quantized step
                                val newNote = TabNote(
                                    stringIndex = stringIndex,
                                    fret = fret,
                                    stepIndex = quantizedStep
                                )
                                addOrReplaceNote(newNote)
                            }
                        }
                    }
                }
            }

            // Metronome click track during recording
            var step = 0
            while (isActive && _isRecording.value) {
                _currentPlaybackStep.value = step

                if (_isClickEnabled.value && (step % _resolution.value.stepsPerBeat == 0)) {
                    val isAccent = (step % _resolution.value.stepsPerBar == 0)
                    toneSynthesizer.playMetronomeTick(isAccent = isAccent, volume = 0.85f)
                }

                delay(stepDurMs)
                step++

                if (step >= totalSteps) {
                    if (_isLooping.value) {
                        step = 0
                    } else {
                        stopRecording()
                        break
                    }
                }
            }

            pitchCollectorJob.cancel()
            pitchDetector.stopListening()
        }
    }

    fun stopRecording() {
        _isCountingIn.value = false
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        pitchDetector.stopListening()
        _currentPlaybackStep.value = -1
        _liveDetectedPluck.value = null
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Interactive Tab Editing
    // ─────────────────────────────────────────────────────────────────────────────

    fun selectNote(noteId: String?) {
        _selectedNoteId.value = noteId
        if (noteId != null) {
            val note = _notes.value.find { it.id == noteId }
            if (note != null) {
                _activeStringIndex.value = note.stringIndex
                _activeStepIndex.value = note.stepIndex
                _activeFret.value = note.fret
                _isEditorTrayOpen.value = true
                previewNote(note.stringIndex, note.fret)
            }
        }
    }

    fun closeEditorTray() {
        _isEditorTrayOpen.value = false
        _selectedNoteId.value = null
        _multiSelectedNoteIds.value = emptySet()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Multi-Select Mode for Bulk Actions (e.g. Bulk Delete)
    // ─────────────────────────────────────────────────────────────────────────────

    fun enterMultiSelect(initialNoteId: String) {
        _multiSelectedNoteIds.value = setOf(initialNoteId)
        _selectedNoteId.value = initialNoteId
        _isEditorTrayOpen.value = true
        val note = _notes.value.find { it.id == initialNoteId }
        if (note != null) {
            _activeStringIndex.value = note.stringIndex
            _activeStepIndex.value = note.stepIndex
            _activeFret.value = note.fret
            previewNote(note.stringIndex, note.fret)
        }
    }

    fun toggleMultiSelectNote(noteId: String) {
        val current = _multiSelectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) {
            current.remove(noteId)
        } else {
            current.add(noteId)
            val note = _notes.value.find { it.id == noteId }
            if (note != null) {
                _activeStringIndex.value = note.stringIndex
                _activeStepIndex.value = note.stepIndex
                _activeFret.value = note.fret
                previewNote(note.stringIndex, note.fret)
            }
        }
        _multiSelectedNoteIds.value = current
        _isEditorTrayOpen.value = true
        if (current.isNotEmpty()) {
            _selectedNoteId.value = current.last()
        } else {
            _selectedNoteId.value = null
        }
    }

    fun clearMultiSelect() {
        _multiSelectedNoteIds.value = emptySet()
    }

    fun deleteMultiSelectedNotes() {
        val idsToDelete = _multiSelectedNoteIds.value
        if (idsToDelete.isEmpty()) return
        pushUndoState()
        val remaining = _notes.value.filter { it.id !in idsToDelete }
        _notes.value = remaining
        _multiSelectedNoteIds.value = emptySet()
        _selectedNoteId.value = null
        // Tray stays displayed showing the current string and fret
        _isEditorTrayOpen.value = true
    }

    fun addOrReplaceNote(newNote: TabNote) {
        pushUndoState()
        val current = _notes.value.toMutableList()
        current.removeAll { it.stringIndex == newNote.stringIndex && it.stepIndex == newNote.stepIndex }
        current.add(newNote)
        _notes.value = current
        _activeStringIndex.value = newNote.stringIndex
        _activeStepIndex.value = newNote.stepIndex
        _activeFret.value = newNote.fret
        _selectedNoteId.value = newNote.id
        _isEditorTrayOpen.value = true
    }

    fun toggleOrAddNoteAt(stringIndex: Int, stepIndex: Int) {
        _activeStringIndex.value = stringIndex
        _activeStepIndex.value = stepIndex
        _isEditorTrayOpen.value = true
        val current = _notes.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.stringIndex == stringIndex && it.stepIndex == stepIndex }

        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            _selectedNoteId.value = existing.id
            _activeFret.value = existing.fret
            previewNote(stringIndex, existing.fret)
        } else {
            // Add a new note (default fret 0)
            pushUndoState()
            val created = TabNote(
                stringIndex = stringIndex,
                fret = 0,
                stepIndex = stepIndex
            )
            current.add(created)
            _selectedNoteId.value = created.id
            _activeFret.value = 0
            previewNote(stringIndex, 0)
            _notes.value = current
        }
    }

    fun updateSelectedNoteFret(newFret: Int) {
        val clampedFret = newFret.coerceIn(0, 24)
        _activeFret.value = clampedFret
        _isEditorTrayOpen.value = true
        val selectedId = _selectedNoteId.value
        val current = _notes.value.toMutableList()
        val idx = if (selectedId != null) current.indexOfFirst { it.id == selectedId } else -1
        if (idx >= 0) {
            pushUndoState()
            val updated = current[idx].copy(fret = clampedFret)
            current[idx] = updated
            _notes.value = current
            previewNote(updated.stringIndex, updated.fret)
        } else {
            // If no note exists at this active slot, create one with the selected fret!
            pushUndoState()
            val newNote = TabNote(
                stringIndex = _activeStringIndex.value,
                stepIndex = _activeStepIndex.value,
                fret = clampedFret
            )
            current.removeAll { it.stringIndex == newNote.stringIndex && it.stepIndex == newNote.stepIndex }
            current.add(newNote)
            _selectedNoteId.value = newNote.id
            _notes.value = current
            previewNote(newNote.stringIndex, newNote.fret)
        }
    }

    fun stepSelectedNoteFret(delta: Int) {
        updateSelectedNoteFret(_activeFret.value + delta)
    }

    fun shiftSelectedNoteString(delta: Int) {
        val maxIndex = strings.size - 1
        val newStringIndex = (_activeStringIndex.value + delta).coerceIn(0, maxIndex)
        _activeStringIndex.value = newStringIndex
        _isEditorTrayOpen.value = true
        val selectedId = _selectedNoteId.value
        val current = _notes.value.toMutableList()
        val idx = if (selectedId != null) current.indexOfFirst { it.id == selectedId } else -1
        if (idx >= 0) {
            pushUndoState()
            val note = current[idx]
            val updated = note.copy(stringIndex = newStringIndex)
            current[idx] = updated
            _notes.value = current
            previewNote(updated.stringIndex, updated.fret)
        } else {
            previewNote(newStringIndex, _activeFret.value)
        }
    }

    fun moveNote(noteId: String, newStringIndex: Int, newStepIndex: Int): Boolean {
        val current = _notes.value.toMutableList()
        val noteIdx = current.indexOfFirst { it.id == noteId }
        if (noteIdx < 0) return false
        val note = current[noteIdx]

        val clampedString = newStringIndex.coerceIn(0, strings.size - 1)
        val clampedStep = newStepIndex.coerceAtLeast(0)

        // If dropping onto same slot, treat as successful no-op
        if (note.stringIndex == clampedString && note.stepIndex == clampedStep) {
            return true
        }

        // Blank spaces only: cannot overwrite existing note
        val isTargetOccupied = current.any { it.id != noteId && it.stringIndex == clampedString && it.stepIndex == clampedStep }
        if (isTargetOccupied) {
            return false
        }

        pushUndoState()
        val moved = note.copy(stringIndex = clampedString, stepIndex = clampedStep)
        current[noteIdx] = moved
        _notes.value = current
        _activeStringIndex.value = clampedString
        _activeStepIndex.value = clampedStep
        _activeFret.value = moved.fret
        _selectedNoteId.value = moved.id
        _isEditorTrayOpen.value = true
        previewNote(clampedString, moved.fret)
        return true
    }

    fun deleteSelectedNote() {
        val selectedId = _selectedNoteId.value
        val currentNotes = _notes.value
        if (selectedId != null) {
            val noteToDelete = currentNotes.find { it.id == selectedId }
            if (noteToDelete != null) {
                _activeStringIndex.value = noteToDelete.stringIndex
                _activeStepIndex.value = noteToDelete.stepIndex
                _activeFret.value = noteToDelete.fret
            }
            pushUndoState()
            _notes.value = currentNotes.filter { it.id != selectedId }
            _multiSelectedNoteIds.value = _multiSelectedNoteIds.value.filter { it != selectedId }.toSet()
            _selectedNoteId.value = null
        } else {
            // Check if note exists at current active slot
            val noteAtSlot = currentNotes.find {
                it.stringIndex == _activeStringIndex.value && it.stepIndex == _activeStepIndex.value
            }
            if (noteAtSlot != null) {
                pushUndoState()
                _notes.value = currentNotes.filter { it.id != noteAtSlot.id }
                _multiSelectedNoteIds.value = _multiSelectedNoteIds.value.filter { it != noteAtSlot.id }.toSet()
            }
        }
        // CRITICAL: The bottom tray 'string ... fret ...' STAYS being displayed, not hidden!
        _isEditorTrayOpen.value = true
    }

    fun deleteNoteById(noteId: String) {
        val currentNotes = _notes.value
        val noteToDelete = currentNotes.find { it.id == noteId }
        if (noteToDelete != null) {
            _activeStringIndex.value = noteToDelete.stringIndex
            _activeStepIndex.value = noteToDelete.stepIndex
            _activeFret.value = noteToDelete.fret
        }
        pushUndoState()
        _notes.value = currentNotes.filter { it.id != noteId }
        _multiSelectedNoteIds.value = _multiSelectedNoteIds.value.filter { it != noteId }.toSet()
        if (_selectedNoteId.value == noteId) {
            _selectedNoteId.value = null
        }
        _isEditorTrayOpen.value = true
    }

    fun clearAllNotes() {
        stopPlayback()
        stopRecording()
        pushUndoState()
        _notes.value = emptyList()
        _selectedNoteId.value = null
    }

    fun previewNote(stringIndex: Int, fret: Int) {
        val tempNote = TabNote(stringIndex = stringIndex, fret = fret, stepIndex = 0)
        toneSynthesizer.playPluckedTone(tempNote.getFrequency(strings), durationSec = 0.9, volume = 0.85f)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Controls & Settings
    // ─────────────────────────────────────────────────────────────────────────────

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(40, 240)
    }

    fun stepBpm(delta: Int) {
        setBpm(_bpm.value + delta)
    }

    fun setResolution(newResolution: TabGridResolution) {
        if (_resolution.value == newResolution) return
        stopPlayback()
        stopRecording()
        _resolution.value = newResolution
    }

    fun toggleLooping() {
        _isLooping.value = !_isLooping.value
    }

    fun toggleClick() {
        _isClickEnabled.value = !_isClickEnabled.value
    }

    fun toggleCountIn() {
        _isCountInEnabled.value = !_isCountInEnabled.value
    }

    fun onNavigateAway() {
        stopPlayback()
        stopRecording()
    }

    fun release() {
        stopPlayback()
        stopRecording()
    }
}
