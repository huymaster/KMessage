package com.github.huymaster.kmessage

import android.app.Application
import com.github.huymaster.kmessage.core.utils.installModules
import com.github.huymaster.kmessage.di.ApplicationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinApplication
import org.koin.core.context.startKoin

@KoinApplication
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            installModules(ApplicationModule)
        }
    }
}