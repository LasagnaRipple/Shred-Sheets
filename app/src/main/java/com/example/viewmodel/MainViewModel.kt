package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticFeedbackManager
import com.example.audio.PitchDetector
import com.example.audio.ToneSynthesizer
import com.example.data.SettingsPreferences
import com.example.model.AppSettings
import com.example.model.AppStyleTheme
import com.example.model.ChordItem
import com.example.model.ColorMode
import com.example.model.InstrumentConfig
import com.example.model.InstrumentRepository
import com.example.model.InstrumentString
import com.example.model.InstrumentType
import com.example.model.MusicalPitchHelper
import com.example.model.PitchResult
import com.example.model.TuningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab(val title: String, val icon: String = "") {
    TUNER("TUNER", ""),
    CHORDS("CHORDS", ""),
    METRONOME("CLICK", ""),
    LOOP("LOOPS", "")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPreferences = SettingsPreferences(application)

    private val pitchDetector = PitchDetector()
    val toneSynthesizer = ToneSynthesizer()
    val hapticManager = HapticFeedbackManager(application)
    val loopStationEngine = com.example.audio.LoopStationEngine(application)

    // App Settings loaded from persistent storage (Auto detect default ON on load/return)
    private val _settings = MutableStateFlow(
        settingsPreferences.loadSettings().copy(tunerMode = com.example.model.TunerMode.AUTO)
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.TUNER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Pitch Detection State
    val pitchState: StateFlow<PitchResult> = pitchDetector.pitchState
    val lastPluckEvent: StateFlow<Long> = pitchDetector.lastPluckEvent

    // Pair of (targetStringNumber, timestamp) to trigger visual pluck shake on tuning bar tick
    private val _pluckAnimationEvent = MutableStateFlow<Pair<Int, Long>?>(null)
    val pluckAnimationEvent: StateFlow<Pair<Int, Long>?> = _pluckAnimationEvent.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Tuner Active State: ready-to-tune waiting state vs active listening state
    private val _isTunerActive = MutableStateFlow(false)
    val isTunerActive: StateFlow<Boolean> = _isTunerActive.asStateFlow()

    private var isAppInForeground = true

    // Selected Target String (for manual mode or closest target in auto mode)
    private val _selectedString = MutableStateFlow<InstrumentString?>(null)
    val selectedString: StateFlow<InstrumentString?> = _selectedString.asStateFlow()

    // String number that just locked in-tune
    private val _activeInTuneStringNumber = MutableStateFlow<Int?>(null)
    val activeInTuneStringNumber: StateFlow<Int?> = _activeInTuneStringNumber.asStateFlow()

    // Metronome State
    private val _metronomeBpm = MutableStateFlow(100)
    val metronomeBpm: StateFlow<Int> = _metronomeBpm.asStateFlow()

    private val _metronomePlaying = MutableStateFlow(false)
    val metronomePlaying: StateFlow<Boolean> = _metronomePlaying.asStateFlow()

    private val _metronomeCurrentBeat = MutableStateFlow(1)
    val metronomeCurrentBeat: StateFlow<Int> = _metronomeCurrentBeat.asStateFlow()

    private val _metronomeTimeSignature = MutableStateFlow(4)
    val metronomeTimeSignature: StateFlow<Int> = _metronomeTimeSignature.asStateFlow()

    // Metronome Sound Mode: CLICK vs DRUMS (defaults to CLICK)
    private val _metronomeSoundMode = MutableStateFlow(com.example.model.MetronomeSoundMode.CLICK)
    val metronomeSoundMode: StateFlow<com.example.model.MetronomeSoundMode> = _metronomeSoundMode.asStateFlow()

    // Drum Style: ROCK, POP, FOLK, LATIN, SHUFFLE (defaults to ROCK)
    private val _drumStyle = MutableStateFlow(com.example.model.DrumStyle.ROCK)
    val drumStyle: StateFlow<com.example.model.DrumStyle> = _drumStyle.asStateFlow()

    private var metronomeJob: Job? = null
    private var loopProgressJob: Job? = null

    // Tap tempo timestamps
    private val tapTimestamps = mutableListOf<Long>()

    // In-tune chime trigger tracker
    private var lastInTuneSoundTime = 0L

    // Tuned strings progress & step guidance
    private val _tunedStringNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val tunedStringNumbers: StateFlow<Set<Int>> = _tunedStringNumbers.asStateFlow()

    private val _promptNextString = MutableStateFlow<InstrumentString?>(null)
    val promptNextString: StateFlow<InstrumentString?> = _promptNextString.asStateFlow()

    private val _isAllStringsTuned = MutableStateFlow(false)
    val isAllStringsTuned: StateFlow<Boolean> = _isAllStringsTuned.asStateFlow()

    private val _recheckingStringNumber = MutableStateFlow<Int?>(null)
    val recheckingStringNumber: StateFlow<Int?> = _recheckingStringNumber.asStateFlow()

    // In-tune dwell timer & confirmation event (per In-Tune Confirmation Spec)
    private var dwellJob: Job? = null
    private var dwellCandidateStringNumber: Int? = null

    // Re-detune sustained out-of-tune tracking job
    private var detuneJob: Job? = null
    private var detuneCandidateStringNumber: Int? = null

    private val _isStringConfirmed = MutableStateFlow(false)
    val isStringConfirmed: StateFlow<Boolean> = _isStringConfirmed.asStateFlow()

    // Active Dialog Visibility
    private val _showInstrumentPicker = MutableStateFlow(false)
    val showInstrumentPicker: StateFlow<Boolean> = _showInstrumentPicker.asStateFlow()

    private val _showTuningPicker = MutableStateFlow(false)
    val showTuningPicker: StateFlow<Boolean> = _showTuningPicker.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showAnagramDialog = MutableStateFlow(false)
    val showAnagramDialog: StateFlow<Boolean> = _showAnagramDialog.asStateFlow()

    // Microphone Permission State
    private val _hasMicPermission = MutableStateFlow(false)
    val hasMicPermission: StateFlow<Boolean> = _hasMicPermission.asStateFlow()

    init {
        // Pre-warm audio synthesizer for instantaneous sub-10ms response when tapping strings
        toneSynthesizer.warmUp()

        // Initialize default string from guitar standard
        val currentTuning = getCurrentTuning()
        _selectedString.value = currentTuning.strings.firstOrNull()

        // Observe pitch detector results to handle in-tune audio and haptics
        viewModelScope.launch {
            pitchState.collect { result ->
                val allStrings = getCurrentTuning().strings

                if (result.frequency > 20.0 && result.confidence > 0.45) {
                    // String strike haptic feedback (disabled when drum loop / click track is playing back)
                    if (_settings.value.hapticsEnabled && !_metronomePlaying.value) {
                        hapticManager.performStringPluckFeedback()
                    }

                    // Re-detuning check for already-done strings:
                    // If pitch drifts out of ±5 cents for >= 200ms sustained, revert string to untuned
                    val closestString = MusicalPitchHelper.findClosestString(result.frequency, allStrings)
                    val centsFromClosest = MusicalPitchHelper.calculateCentsDiff(result.frequency, closestString.targetFrequency)
                    if (_tunedStringNumbers.value.contains(closestString.stringNumber)) {
                        if (kotlin.math.abs(centsFromClosest) > 5.0 && kotlin.math.abs(centsFromClosest) < 150.0) {
                            if (detuneCandidateStringNumber != closestString.stringNumber || detuneJob == null || !detuneJob!!.isActive) {
                                detuneCandidateStringNumber = closestString.stringNumber
                                detuneJob?.cancel()
                                detuneJob = viewModelScope.launch {
                                    delay(200) // 150-200ms sustained debounce
                                    _tunedStringNumbers.value = _tunedStringNumbers.value - closestString.stringNumber
                                    _isAllStringsTuned.value = false
                                    detuneJob = null
                                    detuneCandidateStringNumber = null
                                }
                            }
                        } else if (kotlin.math.abs(centsFromClosest) <= 5.0) {
                            // Still in tune, cancel pending detune
                            if (detuneCandidateStringNumber == closestString.stringNumber) {
                                detuneJob?.cancel()
                                detuneJob = null
                                detuneCandidateStringNumber = null
                            }
                        }
                    }

                    // In Auto mode, find closest string in tuning
                    if (_settings.value.tunerMode == com.example.model.TunerMode.AUTO) {
                        val currentSelected = _selectedString.value
                        val isDifferentString = currentSelected == null || currentSelected.stringNumber != closestString.stringNumber
                        // Only switch if no dwell timer is actively running, or if pitch is clearly closer to another string
                        if (dwellJob == null || !dwellJob!!.isActive || kotlin.math.abs(centsFromClosest) < 50.0) {
                            if (isDifferentString) {
                                // Switching strings cancels dwell timer without penalty
                                dwellJob?.cancel()
                                dwellJob = null
                                dwellCandidateStringNumber = null
                                _isStringConfirmed.value = false
                                _selectedString.value = closestString
                            }
                        }
                    }

                    val targetStr = _selectedString.value ?: allStrings.firstOrNull()
                    val targetNum = targetStr?.stringNumber

                    // In-tune dwell check: ±5 cents threshold
                    val isInTuneTarget = targetStr != null && kotlin.math.abs(result.centsDiff) <= 5.0

                    if (isInTuneTarget && targetNum != null) {
                        _activeInTuneStringNumber.value = targetNum

                        // Start dwell timer (600ms) if not already confirmed or running
                        if (!_tunedStringNumbers.value.contains(targetNum)) {
                            if (dwellCandidateStringNumber != targetNum || dwellJob == null || !dwellJob!!.isActive) {
                                dwellCandidateStringNumber = targetNum
                                dwellJob?.cancel()
                                dwellJob = viewModelScope.launch {
                                    delay(600) // 500-700ms dwell timer
                                    onStringDwellCompleted(targetNum, targetStr)
                                }
                            }
                        }
                    } else {
                        _activeInTuneStringNumber.value = null
                        // If pitch clearly departs the target range (> 5 cents), cancel dwell timer
                        if (kotlin.math.abs(result.centsDiff) > 5.0) {
                            dwellJob?.cancel()
                            dwellJob = null
                            dwellCandidateStringNumber = null
                        }
                    }
                } else {
                    _activeInTuneStringNumber.value = null
                }
            }
        }

        // Observe pluck attacks from pitch detector to trigger tick shake animation
        viewModelScope.launch {
            lastPluckEvent.collect { timestamp ->
                if (timestamp > 0L) {
                    val targetNum = _selectedString.value?.stringNumber
                    if (targetNum != null) {
                        _pluckAnimationEvent.value = Pair(targetNum, timestamp)
                    }
                }
            }
        }
    }

    /**
     * Dwell timer completed: string held in tune (±5 cents) for full duration!
     * Triggers distinct confirmation haptic, chime, confirmed animation, and auto-advances.
     */
    private fun onStringDwellCompleted(targetNum: Int, targetStr: InstrumentString) {
        dwellJob = null
        dwellCandidateStringNumber = null

        // 1. Distinct lock-in double-pulse haptic (disabled when drum loop / click track is playing back)
        if (_settings.value.hapticsEnabled && !_metronomePlaying.value) {
            hapticManager.performConfirmationLockedFeedback()
        }

        // 2. Short success chime (respects sound toggle)
        if (_settings.value.soundEffectsEnabled) {
            toneSynthesizer.playInTuneChime(targetStr.targetFrequency)
        }

        // 3. Fire one-shot confirmed animation burst in circle visualizer
        _isStringConfirmed.value = true

        // 4. Mark string "Done" in the string strip
        val allStrings = getCurrentTuning().strings
        val updatedTuned = _tunedStringNumbers.value + targetNum
        _tunedStringNumbers.value = updatedTuned
        _recheckingStringNumber.value = null

        // 5. Completion or Auto-Advance
        if (updatedTuned.size >= allStrings.size) {
            _isAllStringsTuned.value = true
            _promptNextString.value = null
            if (_settings.value.soundEffectsEnabled) {
                toneSynthesizer.playAllStringsCompletionCelebration()
            }
            viewModelScope.launch {
                delay(1200)
                _isStringConfirmed.value = false
            }
        } else {
            _isAllStringsTuned.value = false
            val currentIndex = allStrings.indexOfFirst { it.stringNumber == targetNum }
            val nextStr = findNextLogicalString(currentIndex, allStrings, updatedTuned)
            _promptNextString.value = nextStr

            viewModelScope.launch {
                // ~500ms pause so user clearly sees the confirmed state before advancing
                delay(500)
                if (nextStr != null) {
                    _selectedString.value = nextStr
                }
                delay(300)
                _isStringConfirmed.value = false
            }
        }
    }

    private fun findNextLogicalString(
        currentIndex: Int,
        strings: List<InstrumentString>,
        tunedStrings: Set<Int>
    ): InstrumentString? {
        if (tunedStrings.size >= strings.size) return null
        if (currentIndex >= 0) {
            for (step in 1 until strings.size) {
                val candidate = strings[(currentIndex + step) % strings.size]
                if (!tunedStrings.contains(candidate.stringNumber)) {
                    return candidate
                }
            }
        }
        return strings.firstOrNull { !tunedStrings.contains(it.stringNumber) }
    }

    fun setAppForeground(isForeground: Boolean) {
        isAppInForeground = isForeground
        if (isForeground) {
            // When returning to the app, Auto detect is toggled to ON
            _settings.value = _settings.value.copy(tunerMode = com.example.model.TunerMode.AUTO)
            toneSynthesizer.warmUp()
        } else {
            stopStringHoldLoop()
            toneSynthesizer.release()
        }
        updateListeningState()
    }

    fun setMicPermission(granted: Boolean) {
        _hasMicPermission.value = granted
        updateListeningState()
    }

    fun updateListeningState() {
        val shouldListen = _hasMicPermission.value &&
                isAppInForeground &&
                _settings.value.isOnboardingCompleted &&
                _currentTab.value == AppTab.TUNER &&
                !_showSettingsDialog.value &&
                _isTunerActive.value

        if (shouldListen) {
            if (!_isListening.value) {
                _isListening.value = true
                pitchDetector.startListening(viewModelScope)
            }
        } else {
            if (_isListening.value) {
                _isListening.value = false
                pitchDetector.stopListening()
            }
        }
    }

    fun activateTuner() {
        _isTunerActive.value = true
        updateListeningState()
        if (_settings.value.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun toggleTunerActive() {
        _isTunerActive.value = !_isTunerActive.value
        updateListeningState()
        if (_settings.value.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun startListening() {
        _isTunerActive.value = true
        updateListeningState()
    }

    fun stopListening() {
        _isListening.value = false
        pitchDetector.stopListening()
    }

    fun selectTab(tab: AppTab) {
        stopStringHoldLoop()
        val previousTab = _currentTab.value
        _currentTab.value = tab
        if (tab == AppTab.TUNER && previousTab != AppTab.TUNER) {
            // When returning to the tuner tab, toggle Auto detect to ON
            _settings.value = _settings.value.copy(tunerMode = com.example.model.TunerMode.AUTO)
        }
        updateListeningState()
        if (_settings.value.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun toggleAutoMode() {
        stopStringHoldLoop()
        val current = _settings.value.tunerMode
        val next = if (current == com.example.model.TunerMode.AUTO) com.example.model.TunerMode.MANUAL else com.example.model.TunerMode.AUTO
        _settings.value = _settings.value.copy(tunerMode = next)
        if (_settings.value.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    private var stringHoldLoopJob: kotlinx.coroutines.Job? = null

    /**
     * Starts continuous looping of the string tone at 120 BPM (500ms interval) while finger is held down.
     */
    fun startStringHoldLoop(string: InstrumentString) {
        selectString(string)
        stringHoldLoopJob?.cancel()
        stringHoldLoopJob = viewModelScope.launch {
            // 120 BPM = 60,000 ms / 120 = 500 ms per stroke
            while (isActive) {
                delay(500L)
                if (_settings.value.soundEffectsEnabled) {
                    toneSynthesizer.playPluckedTone(string.targetFrequency)
                }
                _pluckAnimationEvent.value = Pair(string.stringNumber, System.currentTimeMillis())
                if (_settings.value.hapticsEnabled) {
                    hapticManager.performLightTick()
                }
            }
        }
    }

    /**
     * Stops the looping sound when the user releases their finger.
     */
    fun stopStringHoldLoop() {
        stringHoldLoopJob?.cancel()
        stringHoldLoopJob = null
    }

    fun selectString(string: InstrumentString) {
        // Switching strings mid-dwell: cancel timer without penalty
        dwellJob?.cancel()
        dwellJob = null
        dwellCandidateStringNumber = null
        _isStringConfirmed.value = false

        _selectedString.value = string
        _settings.value = _settings.value.copy(tunerMode = com.example.model.TunerMode.MANUAL)
        _promptNextString.value = null

        if (_settings.value.soundEffectsEnabled) {
            toneSynthesizer.playPluckedTone(string.targetFrequency)
        }
        _pluckAnimationEvent.value = Pair(string.stringNumber, System.currentTimeMillis())
        if (_settings.value.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun resetTunedStringsProgress() {
        dwellJob?.cancel()
        dwellJob = null
        dwellCandidateStringNumber = null
        detuneJob?.cancel()
        detuneJob = null
        detuneCandidateStringNumber = null
        _isStringConfirmed.value = false
        _tunedStringNumbers.value = emptySet()
        _promptNextString.value = null
        _isAllStringsTuned.value = false
        _recheckingStringNumber.value = null
        val currentTuning = getCurrentTuning()
        _selectedString.value = currentTuning.strings.firstOrNull()
    }

    fun playReferenceTone() {
        val str = _selectedString.value ?: getCurrentTuning().strings.firstOrNull() ?: return
        toneSynthesizer.playPluckedTone(str.targetFrequency)
        _pluckAnimationEvent.value = Pair(str.stringNumber, System.currentTimeMillis())
        if (_settings.value.hapticsEnabled) {
            hapticManager.performStringPluckFeedback()
        }
    }

    fun strumChord(chord: ChordItem) {
        toneSynthesizer.strumChord(chord.audioFrequencies)
        if (_settings.value.hapticsEnabled) {
            hapticManager.performInTuneSuccessFeedback()
        }
    }

    fun toggleTheme() {
        val isDark = _settings.value.colorMode == com.example.model.ColorMode.DARK
        val allowed = com.example.model.AccentColor.entries.filter { if (isDark) it.darkAllowed else it.lightAllowed }
        val currentIndex = allowed.indexOf(_settings.value.accentColor)
        val nextAccent = if (currentIndex >= 0) allowed[(currentIndex + 1) % allowed.size] else allowed.first()
        val updated = _settings.value.copy(accentColor = nextAccent)
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun setAccentColor(accent: com.example.model.AccentColor) {
        val updated = _settings.value.copy(accentColor = accent)
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun setColorMode(mode: com.example.model.ColorMode) {
        val isDark = mode == com.example.model.ColorMode.DARK
        val currentAccent = _settings.value.accentColor
        val safeAccent = if (isDark) {
            if (currentAccent.darkAllowed) currentAccent else com.example.model.AccentColor.YELLOW
        } else {
            if (currentAccent.lightAllowed) currentAccent else com.example.model.AccentColor.BLUE
        }
        val updated = _settings.value.copy(colorMode = mode, accentColor = safeAccent)
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun toggleSound() {
        val updated = _settings.value.copy(soundEffectsEnabled = !_settings.value.soundEffectsEnabled)
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        settingsPreferences.saveSettings(newSettings)
        updateListeningState()
    }

    fun completeOnboarding(finalSettings: AppSettings) {
        val config = InstrumentRepository.getConfig(finalSettings.selectedInstrument)
        val tuning = config.tunings.firstOrNull { it.id == config.defaultTuningId } ?: config.tunings.first()
        val sentence = if (finalSettings.selectedInstrument == InstrumentType.GUITAR && tuning.id == "guitar_standard") {
            "Eddie Ate Dynamite Good Bye Eddie"
        } else {
            tuning.anagrams.firstOrNull() ?: tuning.defaultSentence
        }
        val completed = finalSettings.copy(
            isOnboardingCompleted = true,
            selectedTuningId = tuning.id,
            currentAnagramSentence = sentence
        )
        _settings.value = completed
        settingsPreferences.saveSettings(completed)
        _selectedString.value = tuning.strings.firstOrNull()
        _isTunerActive.value = false
        updateListeningState()
    }

    fun setInstrument(instrumentType: InstrumentType) {
        val config = InstrumentRepository.getConfig(instrumentType)
        val tuning = config.tunings.firstOrNull { it.id == config.defaultTuningId } ?: config.tunings.first()
        val sentence = if (instrumentType == InstrumentType.GUITAR && tuning.id == "guitar_standard") {
            "Eddie Ate Dynamite Good Bye Eddie"
        } else {
            tuning.anagrams.firstOrNull() ?: tuning.defaultSentence
        }
        val updated = _settings.value.copy(
            selectedInstrument = instrumentType,
            selectedTuningId = tuning.id,
            currentAnagramSentence = sentence
        )
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        _selectedString.value = tuning.strings.firstOrNull()
        _showInstrumentPicker.value = false
        resetTunedStringsProgress()
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun setTuning(tuning: TuningMode) {
        val sentence = if (_settings.value.selectedInstrument == InstrumentType.GUITAR && tuning.id == "guitar_standard") {
            "Eddie Ate Dynamite Good Bye Eddie"
        } else {
            tuning.anagrams.firstOrNull() ?: tuning.defaultSentence
        }
        val updated = _settings.value.copy(
            selectedTuningId = tuning.id,
            currentAnagramSentence = sentence
        )
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        _selectedString.value = tuning.strings.firstOrNull()
        _showTuningPicker.value = false
        resetTunedStringsProgress()
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    /**
     * Randomly picks a new mnemonic from the available pool for this tuning,
     * ensuring it chooses a different random anagram when multiple exist.
     */
    fun cycleNextAnagram() {
        val tuning = getCurrentTuning()
        val list = if (tuning.anagrams.isNotEmpty()) tuning.anagrams else listOf(tuning.defaultSentence)
        val current = _settings.value.currentAnagramSentence
        val nextSentence = if (list.size > 1) {
            val candidates = list.filter { it != current }
            candidates[Random.nextInt(candidates.size)]
        } else {
            list.first()
        }
        val updated = _settings.value.copy(currentAnagramSentence = nextSentence)
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
        if (updated.hapticsEnabled) {
            hapticManager.performLightTick()
        }
    }

    fun saveAnagram(sentence: String) {
        val updated = _settings.value.copy(currentAnagramSentence = sentence)
        _settings.value = updated
        settingsPreferences.saveSettings(updated)
    }

    fun getCurrentConfig(): InstrumentConfig {
        return InstrumentRepository.getConfig(_settings.value.selectedInstrument)
    }

    fun getCurrentTuning(): TuningMode {
        val config = getCurrentConfig()
        return config.tunings.firstOrNull { it.id == _settings.value.selectedTuningId }
            ?: config.tunings.first()
    }

    fun getAnagramWords(): List<String> {
        val tuning = getCurrentTuning()
        val words = _settings.value.currentAnagramSentence.split(" ").filter { it.isNotBlank() }
        return if (words.size == tuning.strings.size) {
            words
        } else {
            tuning.strings.map { it.defaultWord }
        }
    }

    // Metronome Operations
    fun setMetronomeBpm(bpm: Int) {
        val clamped = bpm.coerceIn(30, 240)
        _metronomeBpm.value = clamped
        loopStationEngine.syncLoopDuration(clamped, _metronomeTimeSignature.value)
    }

    fun setMetronomeTimeSignature(beats: Int) {
        _metronomeTimeSignature.value = beats
        _metronomeCurrentBeat.value = 1
        loopStationEngine.syncLoopDuration(_metronomeBpm.value, beats)
    }

    fun setMetronomeSoundMode(mode: com.example.model.MetronomeSoundMode) {
        _metronomeSoundMode.value = mode
        if (mode == com.example.model.MetronomeSoundMode.DRUMS) {
            _drumStyle.value = com.example.model.DrumStyle.ROCK
        }
    }

    fun setDrumStyle(style: com.example.model.DrumStyle) {
        _drumStyle.value = style
    }

    fun toggleMetronome() {
        if (_metronomePlaying.value) {
            stopMetronome()
        } else {
            startMetronome()
        }
    }

    fun startMetronome() {
        if (_metronomePlaying.value) return
        _metronomePlaying.value = true
        _metronomeCurrentBeat.value = 1

        metronomeJob?.cancel()
        metronomeJob = viewModelScope.launch(Dispatchers.Default) {
            var beatCounter = 0
            var nextBeatNano = System.nanoTime()
            while (isActive && _metronomePlaying.value) {
                val bpm = _metronomeBpm.value
                val intervalNanos = (60_000_000_000L / bpm).coerceAtLeast(100_000_000L)
                val current = _metronomeCurrentBeat.value
                val isAccent = current == 1

                if (_settings.value.soundEffectsEnabled) {
                    if (_metronomeSoundMode.value == com.example.model.MetronomeSoundMode.CLICK) {
                        toneSynthesizer.playMetronomeTick(isAccent)
                    } else {
                        val hits = com.example.model.DrumPatternEngine.getHitsForBeat(
                            style = com.example.model.DrumStyle.ROCK,
                            timeSignatureBeats = _metronomeTimeSignature.value,
                            beat = current
                        )
                        toneSynthesizer.playDrumHits(hits)
                    }
                }
                // Haptic feedback is disabled when drum loop / click track is playing back to the user

                val timeSignature = _metronomeTimeSignature.value
                val barIndex = (beatCounter / timeSignature) % com.example.audio.LoopStationEngine.FIXED_LOOP_BARS
                val isLoopStart = (barIndex == 0 && current == 1)

                // Notify LoopStationEngine for quantized record starts and bar position
                loopStationEngine.onBackingBeatFired(current, barIndex, isLoopStart, bpm, timeSignature)

                beatCounter++
                val beats = _metronomeTimeSignature.value
                val nextBeat = if (current >= beats) 1 else current + 1
                _metronomeCurrentBeat.value = nextBeat

                // Drift-free scheduling: compute exact next target timestamp
                nextBeatNano += intervalNanos
                val sleepNano = nextBeatNano - System.nanoTime()
                if (sleepNano > 1_000_000L) {
                    delay(sleepNano / 1_000_000L)
                } else if (sleepNano < -intervalNanos) {
                    // If fell severely behind (e.g. system pause), re-sync base
                    nextBeatNano = System.nanoTime()
                }
            }
        }

        loopProgressJob?.cancel()
        loopProgressJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && _metronomePlaying.value) {
                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
                loopStationEngine.updateLoopPosition(
                    elapsedSec,
                    _metronomeBpm.value,
                    _metronomeTimeSignature.value
                )
                delay(20)
            }
        }

        loopStationEngine.startRealtimeLoopMixer()
    }

    fun stopMetronome() {
        _metronomePlaying.value = false
        metronomeJob?.cancel()
        metronomeJob = null
        loopProgressJob?.cancel()
        loopProgressJob = null
        loopStationEngine.stopRealtimeLoopMixer()
        loopStationEngine.stopRecording()
    }

    // Loop Station Operations
    fun onTrackPadClicked(trackIndex: Int) {
        loopStationEngine.onTrackPadClicked(
            trackIndex = trackIndex,
            bpm = _metronomeBpm.value,
            timeSignature = _metronomeTimeSignature.value,
            isBackingPlaying = _metronomePlaying.value,
            onEnsureBackingStarted = { startMetronome() }
        )
    }

    fun setLoopLengthSeconds(seconds: Float) {
        val snapped = loopStationEngine.calculateSnappedDuration(
            rawSeconds = seconds,
            bpm = _metronomeBpm.value,
            timeSignature = _metronomeTimeSignature.value
        )
        loopStationEngine.setLoopLengthSeconds(snapped)
    }

    fun toggleTrackPlayback(trackIndex: Int) {
        loopStationEngine.toggleTrackPlayback(trackIndex)
    }

    fun setTrackPlayback(trackIndex: Int, enabled: Boolean) {
        loopStationEngine.setTrackPlayback(trackIndex, enabled)
    }

    fun setBackingVolume(vol: Float) {
        loopStationEngine.setBackingVolume(vol)
    }

    fun setTrackVolume(trackIndex: Int, vol: Float) {
        loopStationEngine.setTrackVolume(trackIndex, vol)
    }

    fun cycleTake(trackIndex: Int, direction: Int) {
        loopStationEngine.cycleTake(trackIndex, direction)
    }

    fun setActiveTake(trackIndex: Int, takeId: String) {
        loopStationEngine.setActiveTake(trackIndex, takeId)
    }

    fun updateTakeName(trackIndex: Int, takeId: String, newName: String) {
        loopStationEngine.updateTakeName(trackIndex, takeId, newName)
    }

    fun updateTakeNotes(trackIndex: Int, takeId: String, notes: String) {
        loopStationEngine.updateTakeNotes(trackIndex, takeId, notes)
    }

    fun deleteTake(trackIndex: Int, takeId: String) {
        loopStationEngine.deleteTake(trackIndex, takeId)
    }

    fun previewTake(take: com.example.model.LoopTake) {
        loopStationEngine.previewTake(take)
    }

    fun dismissHeadphoneTip() {
        loopStationEngine.dismissHeadphoneTip()
    }

    fun clearAllTakes() {
        loopStationEngine.clearAllTakes()
    }

    suspend fun bounceMix(): java.io.File {
        return loopStationEngine.bounceMix(
            isDrums = _metronomeSoundMode.value == com.example.model.MetronomeSoundMode.DRUMS,
            drumStyle = _drumStyle.value,
            bpm = _metronomeBpm.value,
            timeSignature = _metronomeTimeSignature.value
        )
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        if (tapTimestamps.size > 4) {
            tapTimestamps.removeAt(0)
        }

        if (_settings.value.hapticsEnabled) {
            hapticManager.performLightTick()
        }

        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                val diff = tapTimestamps[i] - tapTimestamps[i - 1]
                if (diff in 200..2000) {
                    intervals.add(diff)
                }
            }
            if (intervals.isNotEmpty()) {
                val avgInterval = intervals.average()
                val calculatedBpm = (60_000.0 / avgInterval).toInt().coerceIn(40, 240)
                _metronomeBpm.value = calculatedBpm
            }
        }
    }

    fun openInstrumentPicker() { _showInstrumentPicker.value = true }
    fun closeInstrumentPicker() { _showInstrumentPicker.value = false }
    fun openTuningPicker() { _showTuningPicker.value = true }
    fun closeTuningPicker() { _showTuningPicker.value = false }
    fun openSettings() {
        _showSettingsDialog.value = true
        updateListeningState()
    }
    fun closeSettings() {
        _showSettingsDialog.value = false
        updateListeningState()
    }
    fun openAnagramDialog() { _showAnagramDialog.value = true }
    fun closeAnagramDialog() { _showAnagramDialog.value = false }

    override fun onCleared() {
        super.onCleared()
        stopStringHoldLoop()
        toneSynthesizer.release()
        loopStationEngine.release()
        stopListening()
        stopMetronome()
    }
}
