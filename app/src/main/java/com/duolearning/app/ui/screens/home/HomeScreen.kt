package com.duolearning.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.ui.components.DuoLogo
import com.duolearning.app.ui.components.GlassCard
import com.duolearning.app.ui.components.SectionHeader
import com.duolearning.app.ui.components.StatChip
import com.duolearning.app.ui.components.StreakPill
import com.duolearning.app.ui.components.Tag
import com.duolearning.app.ui.components.WeeklyActivity
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoCyan
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoIndigo
import com.duolearning.app.ui.theme.DuoPurple
import com.duolearning.app.ui.theme.DuoPurpleDeep

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

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Soft violet/blue glows behind the content, drawn once and cheap.
        Box(
            Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(DuoPurple.copy(alpha = 0.20f), Color.Transparent),
                        radius = 900f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { HeaderBar(state, onOpenSettings) }

            item {
                Text(
                    greeting(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DuoGrey
                )
                Text(
                    "Let's hit your goal today!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            item { LanguagePill(state, onChooseCourse) }

            item { DailyGoalCard(state) }

            item {
                ContinueLearningCard(state) {
                    state.nextLessonId?.let { onStartLesson(state.courseCode, it) }
                }
            }

            item { SectionHeader("Quick Actions") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Translate", Icons.Filled.Translate, DuoBlue, Modifier.weight(1f), onOpenTranslate)
                    QuickAction("Practice", Icons.Filled.FitnessCenter, DuoPurple, Modifier.weight(1f), onOpenPractice)
                    QuickAction(
                        "Vocabulary", Icons.Filled.MenuBook, DuoCyan, Modifier.weight(1f),
                        onOpenPractice
                    )
                }
            }

            item { SectionHeader("Weekly Activity") }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    WeeklyActivity(
                        labels = state.weekLabels.ifEmpty { listOf("M", "T", "W", "T", "F", "S", "S") },
                        done = state.weekDone.ifEmpty { List(7) { false } },
                        todayIndex = state.todayIndex
                    )
                }
            }

            if (!state.hasCourse) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(
                            "No lessons for this language yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "This language is available in the translator. Tap the language pill above to pick one with lessons.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DuoGrey
                        )
                    }
                }
            }

            state.units.forEach { unit ->
                item {
                    Column(Modifier.padding(top = 6.dp)) {
                        Text(
                            unit.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
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
}

private fun greeting(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun HeaderBar(state: HomeUiState, onOpenSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DuoLogo(30)
        Spacer(Modifier.width(10.dp))
        Text(
            "Duo Learning",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        StreakPill(state.streak)
        Spacer(Modifier.width(8.dp))
        StatChip(icon = {
            Icon(Icons.Filled.Bolt, null, tint = DuoCyan, modifier = Modifier.size(14.dp))
        }, label = "${state.totalXp}")
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Filled.Settings, "Settings", tint = DuoGrey)
        }
    }
}

@Composable
private fun LanguagePill(state: HomeUiState, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(DuoGreen))
        Spacer(Modifier.width(8.dp))
        Text(
            "Learning ${state.courseLanguage?.englishName ?: "a language"}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(6.dp))
        Text("▾", color = DuoGrey)
    }
}

@Composable
private fun DailyGoalCard(state: HomeUiState) {
    val frac = if (state.goal <= 0) 0f else (state.xpToday.toFloat() / state.goal).coerceIn(0f, 1f)
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Daily Goal Progress",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${state.xpToday} / ${state.goal} XP",
                style = MaterialTheme.typography.labelLarge,
                color = DuoGrey
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.10f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(frac)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(DuoGreen, DuoCyan)))
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (frac >= 1f) "Goal reached — nice work!"
            else "${(state.goal - state.xpToday).coerceAtLeast(0)} XP to go to hit your daily goal!",
            style = MaterialTheme.typography.bodyMedium,
            color = DuoGrey
        )
    }
}

@Composable
private fun ContinueLearningCard(state: HomeUiState, onStart: () -> Unit) {
    val unit = state.units.firstOrNull { u -> u.lessons.any { it.id == state.nextLessonId } }
    val lesson = unit?.lessons?.firstOrNull { it.id == state.nextLessonId }
    val done = state.units.sumOf { u -> u.lessons.count { it.completed } }
    val frac = if (state.totalLessons == 0) 0f else done.toFloat() / state.totalLessons
    val shape = RoundedCornerShape(26.dp)

    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(DuoPurpleDeep, DuoPurple, DuoIndigo, DuoBlue)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(18.dp)
    ) {
        Column {
            Text(
                "CONTINUE LEARNING",
                style = MaterialTheme.typography.labelSmall,
                color = DuoCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                unit?.let { "Unit: ${it.title}" } ?: "Start your first lesson",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                lesson?.let { "${it.title}  ·  ${it.kind.lowercase().replaceFirstChar { c -> c.uppercase() }}" }
                    ?: "Pick a course to begin",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f)
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.22f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(frac)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(DuoGreen)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${(frac * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(50))
                    .clickable(enabled = state.nextLessonId != null, onClick = onStart)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.completedLessons == 0) "Start" else "Resume",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.03f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LessonPathRow(lesson: LessonRow, onClick: () -> Unit) {
    val accent = when {
        lesson.completed -> DuoGreen
        lesson.unlocked -> DuoBlue
        else -> DuoGrey
    }
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.055f))
            .border(1.dp, accent.copy(alpha = 0.28f), shape)
            .clickable(enabled = lesson.unlocked, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
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
            Text(
                lesson.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                lesson.kind.lowercase().replaceFirstChar { it.uppercase() } +
                    if (lesson.bestScore > 0) "  ·  best ${lesson.bestScore}%" else "",
                style = MaterialTheme.typography.labelSmall,
                color = DuoGrey
            )
        }
        if (lesson.completed) Tag("Done", DuoGreen)
    }
}
