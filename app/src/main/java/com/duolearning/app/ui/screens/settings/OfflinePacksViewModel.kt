package com.duolearning.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.data.repository.TranslationRepository
import com.duolearning.app.translate.ModelDownloadState
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackRow(
    val code: String,
    val name: String,
    val nativeName: String,
    val downloaded: Boolean,
    val busy: Boolean = false,
    val message: String? = null
)

class OfflinePacksViewModel(
    private val translation: TranslationRepository
) : ViewModel() {

    private val _packs = MutableStateFlow<List<PackRow>>(emptyList())
    val packs: StateFlow<List<PackRow>> = _packs.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val rows = LanguageCatalog.translatable.map { lang ->
            PackRow(
                code = lang.code, name = lang.englishName, nativeName = lang.nativeName,
                downloaded = translation.isModelDownloaded(lang.code)
            )
        }
        _packs.value = rows
    }

    fun download(code: String) = viewModelScope.launch {
        setBusy(code, true, "Downloading…")
        when (val r = translation.ensureModel(code)) {
            is ModelDownloadState.Done -> setBusy(code, false, "Ready offline")
            is ModelDownloadState.AlreadyDownloaded -> setBusy(code, false, "Already installed")
            is ModelDownloadState.Downloading -> setBusy(code, true, "Downloading…")
            is ModelDownloadState.Failed -> setBusy(code, false, r.message)
        }
        refresh()
    }

    fun delete(code: String) = viewModelScope.launch {
        translation.deleteModel(code)
        refresh()
    }

    private fun setBusy(code: String, busy: Boolean, message: String?) {
        _packs.update { rows ->
            rows.map { if (it.code == code) it.copy(busy = busy, message = message) else it }
        }
    }

    companion object {
        val Factory = duoFactory { c -> OfflinePacksViewModel(c.translationRepository) }
    }
}
