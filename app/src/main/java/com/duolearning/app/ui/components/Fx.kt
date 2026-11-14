package com.duolearning.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.duolearning.app.data.prefs.UserSettings
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoCyan
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoOrange
import com.duolearning.app.ui.theme.DuoPurple
import com.duolearning.app.ui.theme.DuoSurfaceHigh

/**
 * Ambient colour blobs behind the content, mirroring the liquid-glass backdrop.
 * [shift] is the scroll offset in px; each blob drifts at its own rate.
 * Blur is a no-op below API 31, where the blobs simply render as soft circles.
 */
@Composable
fun AmbientBlobs(
    modifier: Modifier = Modifier,
    shift: Float = 0f,
    intensity: Float = 1f,
    blurRadius: Dp = 60.dp
) {
    val a = (0.26f * intensity).coerceIn(0f, 1f)
    Box(modifier) {
        Blob(DuoPurple, 250.dp, (-50).dp, (60 + shift * 0.30f).dp, blurRadius, a)
        Blob(DuoCyan, 200.dp, 200.dp, (300 + shift * 0.18f).dp, blurRadius, a * 0.7f)
        Blob(DuoBlue, 230.dp, 30.dp, (520 + shift * 0.12f).dp, blurRadius, a * 0.8f)
        Blob(DuoOrange, 140.dp, 240.dp, (150 + shift * 0.42f).dp, blurRadius, a * 0.45f)
    }
}

@Composable
private fun Blob(color: Color, size: Dp, x: Dp, y: Dp, blurRadius: Dp, alpha: Float) {
    Box(
        Modifier
            .offset(x = x, y = y)
            .size(size)
            .blur(blurRadius)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

/** Quick effects panel, opened from the header gear. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxSheet(
    settings: UserSettings,
    onGlass: (Boolean) -> Unit,
    onAnimations: (Boolean) -> Unit,
    onBlur: (Float) -> Unit,
    onTransparency: (Float) -> Unit,
    onDepth: (Float) -> Unit,
    onBounce: (Float) -> Unit,
    onSpeed: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Effects yahin se adjust karo",
                style = MaterialTheme.typography.bodyMedium,
                color = DuoGrey
            )
            Spacer(Modifier.height(14.dp))

            FxToggle("Liquid glass", settings.glassEffect, onGlass)
            FxToggle("Animations", settings.animationsEnabled, onAnimations)
            FxSlider("Blur", settings.blurPx, 0f, 40f, "px", onBlur)
            FxSlider("Transparency", settings.transparencyPct, 4f, 30f, "%", onTransparency)
            FxSlider("3D depth", settings.depthPct, 0f, 200f, "%", onDepth)
            FxSlider("Bounce", settings.bouncePct, 0f, 200f, "%", onBounce)
            FxSlider("Animation speed", settings.speedPct, 50f, 200f, "%", onSpeed)

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Reset", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun FxToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun FxSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    unit: String,
    onChange: (Float) -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        PreferenceSlider(value = value, valueRange = min..max, onCommit = onChange) { live ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("${live.toInt()}$unit", style = MaterialTheme.typography.bodyMedium, color = DuoCyan)
            }
        }
    }
}
