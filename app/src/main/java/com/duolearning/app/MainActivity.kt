package com.duolearning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duolearning.app.ui.DuoApp
import com.duolearning.app.ui.LocalContainer
import com.duolearning.app.ui.MainViewModel
import com.duolearning.app.ui.theme.DuoLearningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel(factory = MainViewModel.Factory)
            val textScale by vm.textScale.collectAsStateWithLifecycle()

            val container = (application as DuoApplication).container
            DuoLearningTheme {
                // Honour the user's chosen text size on top of the system
                // font scale, for accessibility.
                val base = LocalDensity.current
                CompositionLocalProvider(
                    LocalContainer provides container,
                    LocalDensity provides Density(base.density, base.fontScale * textScale)
                ) {
                    DuoApp()
                }
            }
        }
    }
}
