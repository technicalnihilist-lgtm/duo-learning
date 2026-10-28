package com.duolearning.app.ui.screens.home

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.ui.components.DuoCard
import com.duolearning.app.ui.components.SectionHeader
import com.duolearning.app.ui.components.StreakPill
import com.duolearning.app.ui.components.Tag
import com.duolearning.app.ui.components.XpProgressBar
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoPurple
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun HomeScreen(
    onStartLesson: (String, String) -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenSettings: () -> Unit,
    onChooseCourse: () -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Duo Learning", style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    Text(
                        state.courseLanguage?.let { "Learning ${it.englishName}" } ?: "Choose a course",
                        style = MaterialTheme.typography.bodyMedium, color = DuoGrey
                    )
                }
                StreakPill(state.streak)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = DuoGrey)
                }
            }
        }

        item {
            DuoCard(Modifier.fillMaxWidth(), onClick = onChooseCourse) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(DuoGreen.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.courseLanguage?.nativeName?.take(2) ?: "--",
                            style = MaterialTheme.typography.titleMedium, color = DuoGreen
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            state.courseLanguage?.englishName ?: "No course",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${state.completedLessons} / ${state.totalLessons} lessons complete",
                            style = MaterialTheme.typography.bodyMedium, color = DuoGrey
                        )
                    }
                    Tag("Change", DuoBlue)
                }
            }
        }

        item {
            DuoCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Daily goal", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    Text("${state.totalXp} XP total", style = MaterialTheme.typography.labelSmall, color = DuoGrey)
                }
                Spacer(Modifier.height(10.dp))
                XpProgressBar(state.xpToday, state.goal)
            }
        }

        item {
            Button(
                onClick = {
                    val next = state.nextLessonId
                    if (next != null) onStartLesson(state.courseCode, next)
                },
                enabled = state.nextLessonId != null,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DuoGreen, contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.completedLessons == 0) "Start learning" else "Continue learning",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onOpenPractice,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Quick practice", color = MaterialTheme.colorScheme.onBackground) }
                OutlinedButton(
                    onClick = onOpenTranslate,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Translate, contentDescription = null, tint = DuoBlue,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Translate", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        if (!state.hasCourse) {
            item {
                DuoCard(Modifier.fillMaxWidth()) {
                    Text("No lessons for this language yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This language is available in the translator. Tap the course card above to pick a language with lessons.",
                        style = MaterialTheme.typography.bodyMedium, color = DuoGrey
                    )
                }
            }
        }

        state.units.forEach { unit ->
            item {
                Column(Modifier.padding(top = 8.dp)) {
                    Text(unit.title, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(unit.subtitle, style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
                }
            }
            items(unit.lessons, key = { it.id }) { lesson ->
                LessonPathRow(lesson) {
                    if (lesson.unlocked) onStartLesson(state.courseCode, lesson.id)
                }
            }
        }
    }
}

@Composable
private fun LessonPathRow(lesson: LessonRow, onClick: () -> Unit) {
    val accent = when {
        lesson.completed -> DuoGreen
        lesson.unlocked -> DuoBlue
        else -> DuoGrey
    }
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DuoSurfaceHigh)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(enabled = lesson.unlocked, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when {
                    lesson.completed -> Icons.Filled.CheckCircle
                    lesson.unlocked -> Icons.Filled.PlayArrow
                    else -> Icons.Filled.Lock
                },
                contentDescription = null, tint = accent, modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(lesson.title, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(
                lesson.kind.lowercase().replaceFirstChar { it.uppercase() } +
                    if (lesson.bestScore > 0) "  ·  best ${lesson.bestScore}%" else "",
                style = MaterialTheme.typography.labelSmall, color = DuoGrey
            )
        }
        if (lesson.completed) Tag("Done", DuoGreen)
    }
}
