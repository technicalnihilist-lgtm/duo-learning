package com.duolearning.app.di

import android.content.Context
import com.duolearning.app.audio.TtsManager
import com.duolearning.app.data.local.DuoDatabase
import com.duolearning.app.data.prefs.SelectionBus
import com.duolearning.app.data.prefs.UserPreferences
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.repository.TranslationRepository
import com.duolearning.app.translate.LanguageIdentifier
import com.duolearning.app.translate.MlKitTranslationEngine
import com.duolearning.app.translate.TranslationEngine

/**
 * Hand-rolled dependency container. A DI framework (Hilt/Koin) would add a
 * build-time annotation processor and hundreds of kB to the APK for an app
 * this size; manual wiring is smaller, faster to build and easier to read.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: DuoDatabase by lazy { DuoDatabase.get(appContext) }

    val preferences: UserPreferences by lazy { UserPreferences(appContext) }

    val selectionBus: SelectionBus by lazy { SelectionBus() }

    val translationEngine: TranslationEngine by lazy { MlKitTranslationEngine() }

    val languageIdentifier: LanguageIdentifier by lazy { LanguageIdentifier() }

    val tts: TtsManager by lazy { TtsManager(appContext) }

    val learningRepository: LearningRepository by lazy {
        LearningRepository(database, preferences)
    }

    val translationRepository: TranslationRepository by lazy {
        TranslationRepository(database, translationEngine, languageIdentifier)
    }
}
