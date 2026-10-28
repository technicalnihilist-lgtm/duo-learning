package com.duolearning.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duolearning.app.DuoApplication
import com.duolearning.app.di.AppContainer

/** Builds a ViewModel factory that injects the app's [AppContainer]. */
inline fun <reified VM : ViewModel> duoFactory(
    crossinline create: (AppContainer) -> VM
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DuoApplication
        create(app.container)
    }
}
