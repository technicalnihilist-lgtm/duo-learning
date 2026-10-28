package com.duolearning.app.ui.screens.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.duolearning.app.ui.LocalContainer
import com.duolearning.app.ui.components.EmptyState
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey
import com.duolearning.app.ui.theme.DuoSurface
import com.duolearning.app.ui.theme.DuoSurfaceHigh

@Composable
fun SavedTranslationsScreen(
    onBack: () -> Unit,
    onPractice: () -> Unit,
    vm: TranslateViewModel = viewModel(factory = TranslateViewModel.Factory)
) {
    val saved by vm.saved.collectAsStateWithLifecycle()
    val tts = LocalContainer.current.tts

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
                Text("Saved translations", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            }
        }
        if (saved.isEmpty()) {
            item { EmptyState("Nothing saved yet", "Translate something and tap Save to keep it here.") }
        } else {
            item {
                Button(
                    onClick = onPractice,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DuoGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text("Practise these") }
            }
            items(saved, key = { it.id }) { row ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DuoSurface)
                        .border(1.dp, DuoSurfaceHigh, RoundedCornerShape(14.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(row.sourceText, color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium)
                        Text(row.targetText, color = DuoGreen,
                            style = MaterialTheme.typography.bodyLarge)
                        Text("${row.sourceLang} → ${row.targetLang}",
                            color = DuoGrey, style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { tts.speak(row.targetText, row.targetLang) }) {
                        Icon(Icons.Filled.VolumeUp, "Speak", tint = DuoGreen)
                    }
                    IconButton(onClick = { vm.setSavedFavourite(row.id, !row.favourite) }) {
                        Icon(
                            if (row.favourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            "Favourite",
                            tint = if (row.favourite) DuoGreen else DuoGrey
                        )
                    }
                    IconButton(onClick = { vm.deleteSaved(row.id) }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = DuoGrey)
                    }
                }
            }
        }
    }
}
