package com.duolearning.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duolearning.app.ui.theme.DuoAmber
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoBorder
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoOrange
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoIndigo
import com.duolearning.app.ui.theme.DuoPurple
import com.duolearning.app.ui.theme.DuoSurfaceHigh

/** The interlocking-rings mark used in the app header. */
@Composable
fun DuoLogo(size: Int = 34) {
    Canvas(Modifier.size(size.dp)) {
        val r = this.size.minDimension * 0.30f
        val cy = this.size.height / 2f
        val stroke = Stroke(width = this.size.minDimension * 0.11f, cap = StrokeCap.Round)
        drawCircle(
            color = DuoPurple,
            radius = r,
            center = Offset(this.size.width * 0.36f, cy),
            style = stroke
        )
        drawCircle(
            color = DuoBlue,
            radius = r,
            center = Offset(this.size.width * 0.64f, cy),
            style = stroke
        )
    }
}

/** Translucent "glass" surface: soft fill plus a light hairline border. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val fill = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.035f))
    )
    val edge = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
    )
    val base = modifier
        .clip(shape)
        .background(fill)
        .border(1.dp, edge, shape)
    val click = onClick
    Box(if (click != null) base.clickable { click() } else base) {
        Column(Modifier.padding(18.dp)) { content() }
    }
}

/** Kept as the generic card used across the app; now glass-styled. */
@Composable
fun DuoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) = GlassCard(modifier, onClick, content)

/** Full-width primary action with the violet->blue gradient. */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .height(54.dp)
            .clip(shape)
            .background(
                if (enabled) Brush.horizontalGradient(listOf(DuoPurple, DuoIndigo, DuoBlue))
                else Brush.horizontalGradient(listOf(DuoSurfaceHigh, DuoSurfaceHigh))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Color.White else DuoGrey,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        action?.invoke()
    }
}

/** Flame + count streak chip. */
@Composable
fun StreakPill(days: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(DuoOrange.copy(alpha = 0.22f), DuoAmber.copy(alpha = 0.14f))
                )
            )
            .border(1.dp, DuoOrange.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.LocalFireDepartment, contentDescription = null,
            tint = DuoAmber, modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "$days",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Small rounded stat chip, e.g. XP. */
@Composable
fun StatChip(icon: @Composable () -> Unit, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = DuoGrey)
    }
}

@Composable
fun XpProgressBar(current: Int, goal: Int, modifier: Modifier = Modifier) {
    val frac = if (goal <= 0) 0f else (current.toFloat() / goal).coerceIn(0f, 1f)
    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                "$current / $goal XP",
                style = MaterialTheme.typography.labelSmall,
                color = DuoGrey,
                modifier = Modifier.weight(1f)
            )
            Text("${(frac * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = DuoGreen)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { frac },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
            color = DuoGreen,
            trackColor = Color.White.copy(alpha = 0.10f),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

/** Weekly streak dots: [labels] are the day letters, [done] which days count. */
@Composable
fun WeeklyActivity(labels: List<String>, done: List<Boolean>, todayIndex: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { i, label ->
            val active = done.getOrElse(i) { false }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) Brush.linearGradient(listOf(DuoPurple, DuoBlue))
                            else Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f))
                            )
                        )
                        .border(
                            width = if (i == todayIndex) 2.dp else 1.dp,
                            color = if (i == todayIndex) DuoGreen
                            else Color.White.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) Color.White else DuoGrey,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = DuoGrey,
            textAlign = TextAlign.Center
        )
    }
}

/** Small coloured availability tag. */
@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
