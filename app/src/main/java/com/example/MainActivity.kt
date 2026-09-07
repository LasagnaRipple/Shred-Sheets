package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.ColorMode
import com.example.ui.anagram.AnagramDialog
import com.example.ui.chords.ChordLibraryScreen
import com.example.ui.components.InstrumentPickerDialog
import com.example.ui.components.ShredBottomNavigation
import com.example.ui.components.TuningPickerDialog
import com.example.ui.metronome.MetronomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.settings.SettingsDialog
import com.example.ui.theme.ShredSheetsTheme
import com.example.ui.tuner.TunerScreen
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val settings by viewModel.settings.collectAsState()

            val isDark = when (settings.colorMode) {
                ColorMode.DARK -> true
                ColorMode.LIGHT -> false
                ColorMode.SYSTEM -> isSystemInDarkTheme()
            }

            DisposableEffect(isDark) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                onDispose {}
            }

            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                // Audio permission launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    viewModel.setMicPermission(isGranted)
                }

                LaunchedEffect(Unit) {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    viewModel.setMicPermission(hasPerm)
                    if (settings.isOnboardingCompleted && !hasPerm) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                // Manage listening state during activity lifecycle
                DisposableEffect(Unit) {
                    onDispose {
                        viewModel.stopListening()
                    }
                }

                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setAppForeground(false)
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val pitchResult by viewModel.pitchState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isTunerActive by viewModel.isTunerActive.collectAsState()
    val selectedString by viewModel.selectedString.collectAsState()
    val activeInTuneStringNumber by viewModel.activeInTuneStringNumber.collectAsState()
    val tunedStringNumbers by viewModel.tunedStringNumbers.collectAsState()
    val promptNextString by viewModel.promptNextString.collectAsState()
    val isAllStringsTuned by viewModel.isAllStringsTuned.collectAsState()
    val isStringConfirmed by viewModel.isStringConfirmed.collectAsState()
    val recheckingStringNumber by viewModel.recheckingStringNumber.collectAsState()
    val hasMicPermission by viewModel.hasMicPermission.collectAsState()
    val pluckAnimationEvent by viewModel.pluckAnimationEvent.collectAsState()

    // Metronome states
    val bpm by viewModel.metronomeBpm.collectAsState()
    val metronomePlaying by viewModel.metronomePlaying.collectAsState()
    val currentBeat by viewModel.metronomeCurrentBeat.collectAsState()
    val timeSignature by viewModel.metronomeTimeSignature.collectAsState()

    // Dialog states
    val showInstrumentPicker by viewModel.showInstrumentPicker.collectAsState()
    val showTuningPicker by viewModel.showTuningPicker.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showAnagramDialog by viewModel.showAnagramDialog.collectAsState()

    val currentConfig = viewModel.getCurrentConfig()
    val currentTuning = viewModel.getCurrentTuning()
    val anagramWords = viewModel.getAnagramWords()

    if (!settings.isOnboardingCompleted) {
        OnboardingScreen(
            initialSettings = settings,
            onUpdateTheme = { colorMode, accentColor ->
                viewModel.updateSettings(
                    settings.copy(colorMode = colorMode, accentColor = accentColor)
                )
            },
            onComplete = { completedSettings ->
                viewModel.completeOnboarding(completedSettings)
                if (!hasMicPermission) {
                    onRequestPermission()
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                ShredBottomNavigation(
                    tabs = AppTab.entries,
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "tabTransition") { tab ->
                    when (tab) {
                        AppTab.TUNER -> {
                            TunerScreen(
                                pitchResult = pitchResult,
                                hasSignal = pitchResult.frequency > 20.0 && pitchResult.confidence > 0.45,
                                isListening = isListening,
                                isTunerActive = isTunerActive,
                                instrumentType = settings.selectedInstrument,
                                tuningMode = currentTuning,
                                styleTheme = settings.styleTheme,
                                soundEnabled = settings.soundEffectsEnabled,
                                isAutoMode = settings.tunerMode == com.example.model.TunerMode.AUTO,
                                selectedString = selectedString,
                                activeInTuneStringNumber = activeInTuneStringNumber,
                                tunedStringNumbers = tunedStringNumbers,
                                promptNextString = promptNextString,
                                isAllStringsTuned = isAllStringsTuned,
                                isStringConfirmed = isStringConfirmed,
                                recheckingStringNumber = recheckingStringNumber,
                                pluckAnimationEvent = pluckAnimationEvent,
                                anagramWords = anagramWords,
                                anagramSentence = settings.currentAnagramSentence,
                                onInstrumentClick = { viewModel.openInstrumentPicker() },
                                onTuningClick = { viewModel.openTuningPicker() },
                                onThemeToggle = { viewModel.toggleTheme() },
                                onSoundToggle = { viewModel.toggleSound() },
                                onSettingsClick = { viewModel.openSettings() },
                                onToggleAutoMode = { viewModel.toggleAutoMode() },
                                onStringSelected = { viewModel.selectString(it) },
                                onPlayReferenceTone = { viewModel.playReferenceTone() },
                                onResetTuning = { viewModel.resetTunedStringsProgress() },
                                onAnagramBarClick = { viewModel.openAnagramDialog() },
                                onCycleAnagram = { viewModel.cycleNextAnagram() },
                                onRequestMicrophonePermission = onRequestPermission,
                                hasMicrophonePermission = hasMicPermission,
                                onToggleTunerActive = {
                                    if (!hasMicPermission) {
                                        onRequestPermission()
                                    }
                                    viewModel.toggleTunerActive()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        AppTab.CHORDS -> {
                            ChordLibraryScreen(
                                instrumentType = settings.selectedInstrument,
                                onStrumChord = { chord -> viewModel.strumChord(chord) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        AppTab.METRONOME -> {
                            MetronomeScreen(
                                bpm = bpm,
                                isPlaying = metronomePlaying,
                                currentBeat = currentBeat,
                                timeSignatureBeats = timeSignature,
                                onBpmChange = { viewModel.setMetronomeBpm(it) },
                                onTogglePlay = { viewModel.toggleMetronome() },
                                onTapTempo = { viewModel.tapTempo() },
                                onTimeSignatureChange = { viewModel.setMetronomeTimeSignature(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Pickers & Dialogs
    if (showInstrumentPicker) {
        InstrumentPickerDialog(
            selectedInstrument = settings.selectedInstrument,
            onSelectInstrument = { viewModel.setInstrument(it) },
            onDismiss = { viewModel.closeInstrumentPicker() }
        )
    }

    if (showTuningPicker) {
        TuningPickerDialog(
            instrumentConfig = currentConfig,
            selectedTuningId = settings.selectedTuningId,
            onSelectTuning = { viewModel.setTuning(it) },
            onDismiss = { viewModel.closeTuningPicker() }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onUpdateSettings = { viewModel.updateSettings(it) },
            onDismiss = { viewModel.closeSettings() }
        )
    }

    if (showAnagramDialog) {
        AnagramDialog(
            instrumentConfig = currentConfig,
            tuningMode = currentTuning,
            currentSentence = settings.currentAnagramSentence,
            onSaveSentence = { viewModel.saveAnagram(it) },
            onDismiss = { viewModel.closeAnagramDialog() }
        )
    }
}
