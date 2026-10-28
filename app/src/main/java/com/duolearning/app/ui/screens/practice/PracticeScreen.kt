package com.duolearning.app.ui.screens.practice

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.ui.LocalContainer
import com.duolearning.app.ui.components.DuoCard
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoRed
import com.duolearning.app.ui.theme.DuoSurface
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun PracticeScreen(
    onStartLesson: (String, String) -> Unit,
    onOpenSaved: () -> Unit,
    onChooseCourse: () -> Unit,
    vm: PracticeViewModel = viewModel(factory = PracticeViewModel.Factory)
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val savedCount by vm.savedCount.collectAsStateWithLifecycle()

    when {
        state.finished -> PracticeResults(state) { vm.exit() }
        state.active -> PracticeSession(state, vm)
        else -> PracticeMenu(state, savedCount, vm)
    }
}

@Composable
private fun PracticeMenu(state: PracticeUiState, savedCount: Int, vm: PracticeViewModel) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Practice", style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            Text("Spaced repetition and saved-translation drills",
                style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
        }
        item {
            PracticeOption("Flashcards", "Review words that are due today, scheduled by spaced repetition.",
                DuoGreen) { vm.start(PracticeMode.FLASHCARDS) }
        }
        item {
            PracticeOption("Difficult words", "Drill the vocabulary you get wrong most often.", DuoRed) {
                vm.start(PracticeMode.DIFFICULT)
            }
        }
        item {
            PracticeOption("Saved translations", "$savedCount saved · revise the phrases you kept.",
                DuoBlue) { vm.start(PracticeMode.SAVED_TRANSLATIONS) }
        }
        state.emptyMessage?.let { msg ->
            item {
                DuoCard(Modifier.fillMaxWidth()) {
                    Text(msg, color = DuoGrey)
                }
            }
        }
    }
}

@Composable
private fun PracticeOption(title: String, subtitle: String, accent: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(DuoSurface)
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(accent))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
        }
    }
}

@Composable
private fun PracticeSession(state: PracticeUiState, vm: PracticeViewModel) {
    val card = state.current ?: return
    val tts = LocalContainer.current.tts

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${state.index + 1} / ${state.total}", color = DuoGrey,
                style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { vm.exit() }, shape = RoundedCornerShape(10.dp)) {
                Text("Exit", color = MaterialTheme.colorScheme.onBackground)
            }
        }

        Box(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)).background(DuoSurface)
                .border(1.dp, DuoSurfaceHigh, RoundedCornerShape(22.dp)).padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(card.front, style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                IconButton(onClick = { tts.speak(card.front, card.frontLang) }) {
                    Icon(Icons.Filled.VolumeUp, "Speak", tint = DuoGreen)
                }
                if (state.revealed) {
                    Spacer(Modifier.height(12.dp))
                    Text(card.back, style = MaterialTheme.typography.headlineSmall,
                        color = DuoGreen, textAlign = TextAlign.Center)
                    if (card.hint.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(card.hint, style = MaterialTheme.typography.bodyMedium,
                            color = DuoGrey, textAlign = TextAlign.Center)
                    }
                } else if (card.hint.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(card.hint, style = MaterialTheme.typography.bodyMedium,
                        color = DuoSurfaceHigh, textAlign = TextAlign.Center)
                }
            }
        }

        if (!state.revealed) {
            Button(
                onClick = { vm.reveal() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DuoBlue,
                    contentColor = MaterialTheme.colorScheme.onSecondary)
            ) { Text("Show answer") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { vm.grade(false) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DuoRed,
                        contentColor = MaterialTheme.colorScheme.onError)
                ) { Text("Again") }
                Button(
                    onClick = { vm.grade(true) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DuoGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text("Got it") }
            }
        }
    }
}

@Composable
private fun PracticeResults(state: PracticeUiState, onExit: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Session complete", style = MaterialTheme.typography.displaySmall,
            color = DuoGreen, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("${state.correct} / ${state.total} correct",
            style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("+${state.result?.xpAwarded ?: 0} XP", color = DuoGreen,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DuoGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary)
        ) { Text("Back to practice") }
    }
}
