package com.duolearning.app.ui.screens.learn

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.data.model.ExerciseSpec
import com.duolearning.app.ui.LocalContainer
import com.duolearning.app.ui.components.Tag
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoRed
import com.duolearning.app.ui.theme.DuoSurface
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun LessonScreen(
    course: String,
    lessonId: String,
    onExit: () -> Unit,
    onNextLesson: (String, String) -> Unit,
    vm: LessonViewModel = viewModel(factory = LessonViewModel.factory(course, lessonId))
) {
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.finished) {
        LessonResults(state, onExit = onExit, onNext = {
            val next = state.nextLessonId
            if (next != null) onNextLesson(state.courseCode, next) else onExit()
        })
        return
    }

    Column(Modifier.fillMaxSize()) {
        // Header: close + progress
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Close lesson", tint = DuoGrey)
            }
            val frac = if (state.total == 0) 0f else state.index.toFloat() / state.total
            LinearProgressIndicator(
                progress = { frac },
                modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)),
                color = DuoGreen, trackColor = DuoSurfaceHigh, gapSize = 0.dp, drawStopIndicator = {}
            )
            Spacer(Modifier.width(12.dp))
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.error != null) {
                Text(state.error!!, color = DuoRed)
            } else {
                state.exercise?.let { ex ->
                    Text(
                        ex.prompt,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    ExerciseBody(state, ex, vm)
                }
            }
        }

        FeedbackAndAction(state, vm)
    }
}

@Composable
private fun ExerciseBody(state: LessonUiState, ex: ExerciseSpec, vm: LessonViewModel) {
    val tts = LocalContainer.current.tts
    when (ex) {
        is ExerciseSpec.MultipleChoice, is ExerciseSpec.FillBlank, is ExerciseSpec.Listening,
        is ExerciseSpec.Correction -> {
            if (ex is ExerciseSpec.Listening) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        tts.speak(ex.spoken, state.courseLanguage?.code ?: "en")
                    }) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "Play audio", tint = DuoGreen)
                    }
                    Text("Tap to hear it again", style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
                }
            }
            state.options.forEachIndexed { i, opt ->
                ChoiceRow(
                    text = opt,
                    selected = state.selectedIndex == i,
                    enabled = !state.checked,
                    onClick = { vm.selectOption(i) }
                )
            }
        }

        is ExerciseSpec.TypedTranslation -> {
            OutlinedTextField(
                value = state.textAnswer,
                onValueChange = vm::setTextAnswer,
                enabled = !state.checked,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Your translation") },
                shape = RoundedCornerShape(14.dp)
            )
            if (state.checked && !state.isCorrect) {
                Text("Accepted answer: ${ex.accepted.first()}", color = DuoGreen,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

        is ExerciseSpec.Scrambled -> {
            // Selected sentence
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(DuoSurface).border(1.dp, DuoSurfaceHigh, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    if (state.selectedTokens.isEmpty()) "Tap the words below in order"
                    else state.selectedTokens.joinToString(" "),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.height(4.dp))
            TokenWrap(state.bank, state.selectedTokens, state.checked) { vm.toggleToken(it) }
        }

        is ExerciseSpec.MatchPairs -> {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ex.pairs.forEach { (left, _) ->
                        val matched = state.matches[left]
                        ChoiceRow(
                            text = if (matched != null) "$left  →  $matched" else left,
                            selected = state.selectedLeft == left,
                            enabled = !state.checked,
                            onClick = { vm.selectLeft(left) }
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.rightColumn.forEach { right ->
                        val used = state.matches.values.contains(right)
                        ChoiceRow(
                            text = right,
                            selected = used,
                            enabled = !state.checked && !used,
                            onClick = { vm.matchRight(right) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TokenWrap(
    bank: List<String>,
    selected: List<String>,
    locked: Boolean,
    onToggle: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bank.forEach { token ->
            val used = selected.contains(token)
            Box(
                Modifier.clip(RoundedCornerShape(12.dp))
                    .background(if (used) DuoSurface else DuoSurfaceHigh)
                    .border(1.dp, if (used) DuoSurfaceHigh else DuoGreen.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp))
                    .clickable(enabled = !locked) { onToggle(token) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    token,
                    color = if (used) DuoGrey else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (selected) DuoGreen.copy(alpha = 0.14f) else DuoSurface)
            .border(
                1.5.dp,
                if (selected) DuoGreen else DuoSurfaceHigh,
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun FeedbackAndAction(state: LessonUiState, vm: LessonViewModel) {
    val canCheck = when (state.exercise) {
        is ExerciseSpec.TypedTranslation -> state.textAnswer.isNotBlank()
        is ExerciseSpec.Scrambled -> state.selectedTokens.isNotEmpty()
        is ExerciseSpec.MatchPairs -> state.matches.size == (state.exercise as ExerciseSpec.MatchPairs).pairs.size
        else -> state.selectedIndex >= 0
    }
    Column(
        Modifier.fillMaxWidth().background(DuoSurface).padding(16.dp)
    ) {
        if (state.checked) {
            Text(
                if (state.isCorrect) "Correct" else "Not quite",
                color = if (state.isCorrect) DuoGreen else DuoRed,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            val explanation = state.exercise?.explanation.orEmpty()
            if (explanation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(explanation, color = DuoGrey, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { if (state.checked) vm.next() else vm.check() },
            enabled = state.checked || canCheck,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.checked && !state.isCorrect) DuoRed else DuoGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(if (state.checked) "Continue" else "Check", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun LessonResults(
    state: LessonUiState,
    onExit: () -> Unit,
    onNext: () -> Unit
) {
    val result = state.result
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (result?.passed == true) "Lesson complete" else "Keep practising",
            style = MaterialTheme.typography.displaySmall,
            color = if (result?.passed == true) DuoGreen else DuoRed,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text("${state.correctCount} / ${state.total} correct  ·  ${state.accuracy}%",
            color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("+${result?.xpAwarded ?: 0} XP", color = DuoGreen,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag("${result?.newStreak ?: 0} day streak", DuoGreen)
            if (result?.streakExtended == true) Tag("Streak extended!", DuoGreen)
        }
        if (!result?.unlockedAchievements.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Achievements unlocked", color = MaterialTheme.colorScheme.onBackground)
            result.unlockedAchievements.forEach {
                Text("· ${it.replace('_', ' ')}", color = DuoGrey)
            }
        }
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DuoGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary)
        ) { Text(if (state.nextLessonId != null) "Next lesson" else "Done") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DuoSurfaceHigh,
                contentColor = MaterialTheme.colorScheme.onBackground)
        ) { Text("Back to course") }
    }
}
