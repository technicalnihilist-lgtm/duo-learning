package com.duolearning.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.BuildConfig
import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.ui.components.DuoCard
import com.duolearning.app.ui.components.PreferenceSlider
import com.duolearning.app.ui.components.SectionHeader
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoRed
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenOfflinePacks: () -> Unit,
    onChooseNative: () -> Unit,
    onChooseCourse: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showReset by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DuoGrey)
                }
                Text("Settings", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            }
        }

        item { SectionHeader("Course and language") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                SettingRow(
                    "Native language",
                    LanguageCatalog.byCode(settings.nativeLanguage)?.englishName ?: settings.nativeLanguage
                ) { onChooseNative() }
                SettingRow(
                    "Learning",
                    LanguageCatalog.byCode(settings.activeCourse)?.englishName ?: settings.activeCourse
                ) { onChooseCourse() }
            }
        }

        item { SectionHeader("Daily goal") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                PreferenceSlider(
                    value = settings.dailyGoalXp.toFloat(),
                    valueRange = 10f..200f,
                    steps = 18,
                    onCommit = { vm.setGoal(it.toInt()) }
                ) { live ->
                    Text("${live.toInt()} XP per day",
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        item { SectionHeader("Sound and pronunciation") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Play pronunciation audio",
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    Switch(checked = settings.soundEnabled, onCheckedChange = vm::setSound)
                }
                Spacer(Modifier.height(8.dp))
                PreferenceSlider(
                    value = settings.speechRate,
                    valueRange = 0.5f..2.0f,
                    onCommit = vm::setSpeechRate
                ) { live ->
                    Text("Speech speed: ${"%.1f".format(live)}x", color = DuoGrey)
                }
            }
        }

        item { SectionHeader("Motion and depth") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("4D depth effect", color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "Subtle 3D tilt on cards as you scroll",
                            style = MaterialTheme.typography.labelSmall, color = DuoGrey
                        )
                    }
                    Switch(checked = settings.depthEffect, onCheckedChange = vm::setDepthEffect)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Parallax", color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "Background glow follows your scroll",
                            style = MaterialTheme.typography.labelSmall, color = DuoGrey
                        )
                    }
                    Switch(checked = settings.parallax, onCheckedChange = vm::setParallax)
                }
                Spacer(Modifier.height(10.dp))
                PreferenceSlider(
                    value = settings.effectStrength,
                    valueRange = 0f..1f,
                    onCommit = vm::setEffectStrength
                ) { live ->
                    Text(
                        "Effect strength: ${(live * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium, color = DuoGrey
                    )
                }
            }
        }

        item { SectionHeader("Accessibility") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                PreferenceSlider(
                    value = settings.textScale,
                    valueRange = 0.85f..1.5f,
                    onCommit = vm::setTextScale
                ) { live ->
                    Text("Text size: ${(live * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        item { SectionHeader("Offline translation") }
        item {
            DuoCard(Modifier.fillMaxWidth(), onClick = onOpenOfflinePacks) {
                Text("Offline language packs", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Download on-device models to translate without internet.",
                    style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
            }
        }

        item { SectionHeader("Data and privacy") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                Text(
                    "Duo Learning stores everything on this device. There is no account, no advertising and no analytics. Text is only sent to the on-device translation engine when you press Translate.",
                    style = MaterialTheme.typography.bodyMedium, color = DuoGrey
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { vm.clearHistory() }, shape = RoundedCornerShape(12.dp)) {
                    Text("Clear translation history", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        item {
            Button(
                onClick = { showReset = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DuoRed,
                    contentColor = MaterialTheme.colorScheme.onError)
            ) { Text("Reset all progress") }
        }

        item {
            Text(
                "Duo Learning ${BuildConfig.VERSION_NAME}  ·  Offline-first, account-free language learning.",
                style = MaterialTheme.typography.labelSmall, color = DuoGrey,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset all progress?") },
            text = {
                Text("This permanently deletes your streak, XP, lesson progress, saved translations and achievements. It cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetProgress { }
                    showReset = false
                }) { Text("Reset", color = DuoRed) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Text(value, color = DuoGreen, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OfflinePacksScreen(
    onBack: () -> Unit,
    vm: OfflinePacksViewModel = viewModel(factory = OfflinePacksViewModel.Factory)
) {
    val packs by vm.packs.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DuoGrey)
                }
                Text("Offline language packs", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Text(
                "These are the languages the on-device translation engine supports. Downloading a pack (typically 15-40 MB) lets you translate in airplane mode. Languages not listed here are not supported by the engine.",
                style = MaterialTheme.typography.bodyMedium, color = DuoGrey
            )
        }
        items(packs.size) { i ->
            val pack = packs[i]
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(DuoSurfaceHigh)
                    .border(1.dp, DuoSurfaceHigh, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("${pack.name}  ·  ${pack.nativeName}",
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        pack.message ?: if (pack.downloaded) "Installed" else "Not installed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pack.downloaded) DuoGreen else DuoGrey
                    )
                }
                if (pack.downloaded) {
                    OutlinedButton(onClick = { vm.delete(pack.code) },
                        shape = RoundedCornerShape(10.dp)) {
                        Text("Remove", color = DuoRed)
                    }
                } else {
                    OutlinedButton(onClick = { vm.download(pack.code) },
                        enabled = !pack.busy, shape = RoundedCornerShape(10.dp)) {
                        Text(if (pack.busy) "…" else "Download",
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
    }
}
