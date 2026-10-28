package com.duolearning.app.ui.screens.translate

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.data.local.entity.TranslationHistoryEntity
import com.duolearning.app.ui.LocalContainer
import com.duolearning.app.ui.components.DuoCard
import com.duolearning.app.ui.components.SectionHeader
import com.duolearning.app.ui.components.Tag
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoRed
import com.duolearning.app.ui.theme.DuoSurface
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun TranslateScreen(
    onOpenSaved: () -> Unit,
    onPickLanguage: (String) -> Unit,
    onStartLesson: (String, String) -> Unit,
    vm: TranslateViewModel = viewModel(factory = TranslateViewModel.Factory)
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val tts = LocalContainer.current.tts
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    fun langName(code: String): String = when (code) {
        "auto" -> "Detect language"
        else -> LanguageCatalog.byCode(code)?.englishName ?: code
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Duo Translate", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onOpenSaved, shape = RoundedCornerShape(12.dp)) {
                    Text("Saved (${history.size})", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        item {
            DuoCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LangPill(langName(state.source), Modifier.weight(1f)) { onPickLanguage("source") }
                    IconButton(onClick = { vm.swap() }) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Swap languages", tint = DuoBlue)
                    }
                    LangPill(langName(state.target), Modifier.weight(1f)) { onPickLanguage("target") }
                }
                if (state.detected != null && state.source == "auto") {
                    Spacer(Modifier.height(6.dp))
                    Text("Detected: ${langName(state.detected!!)}",
                        style = MaterialTheme.typography.labelSmall, color = DuoGrey)
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.text,
                onValueChange = vm::setText,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 10,
                placeholder = { Text("Type or paste text…") },
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    clipboard.getText()?.text?.let { vm.append(it) }
                }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Text("Paste", color = MaterialTheme.colorScheme.onBackground)
                }
                OutlinedButton(onClick = { vm.clear() }, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)) {
                    Text("Clear", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        item {
            Button(
                onClick = { vm.translate() },
                enabled = state.text.isNotBlank() && !state.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DuoGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text("Translating…")
                } else {
                    Text("Translate", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        state.resultText?.let { result ->
            item {
                DuoCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Translation", style = MaterialTheme.typography.labelSmall, color = DuoGrey,
                            modifier = Modifier.weight(1f))
                        if (state.isCacheHit) Tag("cached", DuoBlue)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(result, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = {
                            tts.speak(result, state.target)
                        }) { Icon(Icons.Filled.VolumeUp, "Speak translation", tint = DuoGreen) }
                        IconButton(onClick = { clipboard.setText(AnnotatedString(result)) }) {
                            Icon(Icons.Filled.ContentCopy, "Copy", tint = DuoGrey)
                        }
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, result)
                            }
                            context.startActivity(Intent.createChooser(send, "Share translation"))
                        }) { Icon(Icons.Filled.Share, "Share", tint = DuoGrey) }
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = { vm.saveCurrent() }, shape = RoundedCornerShape(12.dp)) {
                            Text("Save", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    if (state.savedJustNow) {
                        Spacer(Modifier.height(6.dp))
                        Tag("Saved to vocabulary", DuoGreen)
                    }
                }
            }
            item {
                LearnThisCard(
                    sourceText = state.text,
                    targetText = result,
                    onSave = { vm.saveCurrent() },
                    onPractice = onOpenSaved
                )
            }
        }

        state.errorText?.let { error ->
            item {
                DuoCard(Modifier.fillMaxWidth()) {
                    Text("Could not translate", color = DuoRed,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(error, color = DuoGrey, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "The on-device engine only handles the languages it ships models for, and a model must be downloaded once. Check Settings → Offline language packs.",
                        color = DuoGrey, style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        if (history.isNotEmpty()) {
            item { SectionHeader("Recent") { Text("", color = DuoGrey) } }
            items(history, key = { it.id }) { row ->
                HistoryRow(row, onCopy = { clipboard.setText(AnnotatedString(row.targetText)) },
                    onSpeak = { tts.speak(row.targetText, row.targetLang) },
                    onDelete = { vm.deleteHistory(row.id) })
            }
        }
    }
}

@Composable
private fun LangPill(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(DuoSurfaceHigh)
            .clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun LearnThisCard(
    sourceText: String,
    targetText: String,
    onSave: () -> Unit,
    onPractice: () -> Unit
) {
    DuoCard(Modifier.fillMaxWidth()) {
        Text("Learn this", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text(sourceText, color = DuoGrey, style = MaterialTheme.typography.bodyMedium)
        Text(targetText, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Word-by-word glosses and grammar notes are shown only when the engine can supply them reliably; the on-device translator does not, so this pair is offered as study material instead.",
            color = DuoGrey, style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onSave, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)) {
                Text("Add to vocabulary", color = MaterialTheme.colorScheme.onBackground)
            }
            OutlinedButton(onClick = onPractice, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)) {
                Text("Practise", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun HistoryRow(
    row: TranslationHistoryEntity,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DuoSurface)
            .border(1.dp, DuoSurfaceHigh, RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.sourceText, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text(row.targetText, color = DuoGrey, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text("${row.sourceLang} → ${row.targetLang}",
                color = DuoGrey, style = MaterialTheme.typography.labelSmall)
        }
        IconButton(onClick = onSpeak) { Icon(Icons.Filled.VolumeUp, "Speak", tint = DuoGreen) }
        IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, "Copy", tint = DuoGrey) }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete", tint = DuoGrey) }
    }
}
