package com.duolearning.app.ui.screens.language

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.data.model.Language
import com.duolearning.app.ui.LocalContainer
import com.duolearning.app.ui.components.Tag
import com.duolearning.app.ui.theme.DuoBlue
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoPurple
import com.duolearning.app.ui.theme.DuoSurface
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun LanguagePickerScreen(
    mode: String,
    onBack: () -> Unit,
    onPicked: () -> Unit
) {
    val bus = LocalContainer.current.selectionBus
    var query by remember { mutableStateOf("") }

    val all = LanguageCatalog.search(query)
    val selectable: (Language) -> Boolean = when (mode) {
        "learn" -> { lang -> lang.courseAvailable }
        "source", "target", "native" -> { lang -> lang.translateSupported }
        else -> { _ -> false }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DuoGrey)
                }
                Text(
                    when (mode) {
                        "learn" -> "Choose a course"
                        "source" -> "Source language"
                        "target" -> "Target language"
                        "native" -> "Native language"
                        else -> "Languages"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = DuoGrey) },
                placeholder = { Text("Search languages") },
                shape = RoundedCornerShape(14.dp)
            )
        }
        item {
            Text(
                "Availability is shown honestly: a language is only selectable here when the app has real content or a real engine model for it.",
                style = MaterialTheme.typography.labelSmall, color = DuoGrey
            )
        }
        items(all, key = { it.code }) { lang ->
            val enabled = selectable(lang)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) DuoSurface else DuoSurfaceHigh)
                    .border(
                        1.dp,
                        if (enabled) DuoSurfaceHigh else DuoSurfaceHigh,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = enabled) {
                        bus.publish(mode, lang.code)
                        onPicked()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        lang.englishName,
                        color = if (enabled) MaterialTheme.colorScheme.onBackground else DuoGrey,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("${lang.nativeName}  ·  ${lang.script}",
                        color = DuoGrey, style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (lang.courseAvailable) Tag("Lessons", DuoGreen)
                    Spacer(Modifier.height(4.dp))
                    if (lang.translateSupported) Tag("Translate", DuoBlue)
                    else Tag("No model", DuoPurple)
                }
            }
        }
    }
}
