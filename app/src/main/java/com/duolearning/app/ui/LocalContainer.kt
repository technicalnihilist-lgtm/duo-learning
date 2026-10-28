package com.duolearning.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.duolearning.app.di.AppContainer

/** Exposes the app's dependency container to the composable tree. */
val LocalContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer was not provided. Wrap the tree in CompositionLocalProvider(LocalContainer provides ...).")
}
