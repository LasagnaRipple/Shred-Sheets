package com.example.ui.anagram

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.InstrumentConfig
import com.example.model.InstrumentString
import com.example.model.TuningMode
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.ShredCardBorder
import com.example.ui.theme.ShredCardSurface
import com.example.ui.theme.ShredMutedText
import com.example.ui.theme.ShredPrimaryText

@Composable
fun AnagramDialog(
    instrumentConfig: InstrumentConfig,
    tuningMode: TuningMode,
    currentSentence: String,
    onSaveSentence: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isCustomMode by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current
    val dialogSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    val dialogBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
    val primaryText = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
    val mutedText = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    // List of words for each string in tuning
    val customWords = remember(tuningMode) {
        val initialWords = currentSentence.split(" ")
        mutableStateListOf<String>().apply {
            tuningMode.strings.forEachIndexed { index, instString ->
                add(initialWords.getOrNull(index) ?: instString.defaultWord)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = dialogSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Info Icon, Title and Close Button (matching Chord Library style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "String Anagrams",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            ),
                            color = primaryText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_anagram_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText
                        )
                    }
                }

                Text(
                    text = "Fun mnemonics to remember your guitar string names!",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedText,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets vs Custom Mode switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isCustomMode) primaryColor.copy(alpha = 0.15f)
                                else if (isDark) ShredCardBorder.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (!isCustomMode) primaryColor.copy(alpha = 0.7f)
                                else if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { isCustomMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fun Presets ⚡",
                            fontWeight = FontWeight.Bold,
                            color = if (!isCustomMode) primaryColor else mutedText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCustomMode) primaryColor.copy(alpha = 0.15f)
                                else if (isDark) ShredCardBorder.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isCustomMode) primaryColor.copy(alpha = 0.7f)
                                else if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { isCustomMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create My Own ✏️",
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomMode) primaryColor else mutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isCustomMode) {
                    // Presets List for currently active tuning
                    val presets = tuningMode.anagrams.ifEmpty { instrumentConfig.defaultAnagrams }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presets) { preset ->
                            val isSelected = currentSentence == preset
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.15f)
                                        else if (isDark) ShredCardBorder.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) primaryColor.copy(alpha = 0.7f)
                                        else if (isDark) ShredCardBorder.copy(alpha = 0.5f)
                                        else Color.Transparent,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onSaveSentence(preset)
                                        onDismiss()
                                    }
                                    .padding(12.dp)
                                    .testTag("preset_anagram_${preset.take(8)}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) primaryColor else primaryText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom Creator: Each string word slot
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(tuningMode.strings.size) { index ->
                            val str = tuningMode.strings[index]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.15f))
                                        .border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = str.noteLetter,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = primaryColor
                                    )
                                }

                                OutlinedTextField(
                                    value = customWords.getOrElse(index) { "" },
                                    onValueChange = { newWord ->
                                        if (index < customWords.size) {
                                            customWords[index] = newWord
                                        }
                                    },
                                    label = { Text("Word starting with '${str.noteLetter}'", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("custom_word_input_$index"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant,
                                        focusedTextColor = primaryText,
                                        unfocusedTextColor = primaryText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val combined = customWords.joinToString(" ")
                            onSaveSentence(combined)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_custom_anagram_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color(0xFF12140F)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Rockstar Anagram 🤙",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF12140F)
                        )
                    }
                }
            }
        }
    }
}
