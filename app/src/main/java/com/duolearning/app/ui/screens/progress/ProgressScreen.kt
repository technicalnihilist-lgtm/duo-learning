package com.duolearning.app.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.duolearning.app.domain.Dates
import com.duolearning.app.ui.components.DuoCard
import com.duolearning.app.ui.components.SectionHeader
import com.duolearning.app.ui.components.StreakPill
import com.duolearning.app.ui.components.XpProgressBar
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun ProgressScreen(
    onOpenSettings: () -> Unit,
    vm: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory)
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val stats = state.stats
    val window = vm.calendarWindow()
    val qualifiedDays = state.activity.filter { it.qualified }.map { it.epochDay }.toSet()
    val today = Dates.todayEpochDay()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Progress", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = DuoGrey)
                }
            }
        }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakPill(stats.currentStreak)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Current streak", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text("Longest: ${stats.longestStreak} days",
                            style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
                    }
                }
                Spacer(Modifier.height(12.dp))
                XpProgressBar(state.xpToday, stats.dailyGoal)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total XP", "${stats.totalXp}", Modifier.weight(1f))
                StatCard("Lessons", "${stats.lessonsCompleted}", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Words", "${stats.wordsLearned}", Modifier.weight(1f))
                StatCard("Badges", "${stats.achievementsEarned}/${stats.achievementsTotal}", Modifier.weight(1f))
            }
        }
        item { SectionHeader("Learning calendar") }
        item {
            DuoCard(Modifier.fillMaxWidth()) {
                // 7 columns x 5 rows of days.
                window.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { day ->
                            val done = day in qualifiedDays
                            val isToday = day == today
                            Box(
                                Modifier.weight(1f).height(30.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (done) DuoGreen.copy(alpha = 0.85f) else DuoSurfaceHigh)
                                    .border(
                                        1.dp,
                                        if (isToday) DuoGreen else DuoSurfaceHigh,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${Dates.dayOfMonth(day)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (done) MaterialTheme.colorScheme.onPrimary else DuoGrey
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        item { SectionHeader("Achievements") }
        items(state.achievements, key = { it.id }) { row ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(DuoSurfaceHigh)
                    .border(1.dp, if (row.earned) DuoGreen.copy(alpha = 0.4f) else DuoSurfaceHigh,
                        RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (row.earned) DuoGreen.copy(alpha = 0.18f) else DuoSurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (row.earned) "✓" else "•",
                        color = if (row.earned) DuoGreen else DuoGrey)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(row.title, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(row.description, style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    DuoCard(modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = DuoGreen,
            fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DuoGrey)
    }
}
