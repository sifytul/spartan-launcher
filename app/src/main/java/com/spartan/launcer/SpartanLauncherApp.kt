package com.spartan.launcer

import android.app.Application
import com.spartan.launcer.di.AppContainer
import kotlinx.coroutines.launch

class SpartanLauncherApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.appScope.launch {
            container.appRepository.refresh()
        }
    }
}