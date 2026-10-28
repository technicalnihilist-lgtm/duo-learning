package com.duolearning.app

import android.app.Application
import com.duolearning.app.di.AppContainer

class DuoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
