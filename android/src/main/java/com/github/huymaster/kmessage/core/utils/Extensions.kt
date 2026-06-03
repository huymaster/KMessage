package com.github.huymaster.kmessage.core.utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.github.huymaster.kmessage.di.KoinModuleProvider
import org.koin.core.KoinApplication

val Context.authDataStore by preferencesDataStore("auth_prefs")

fun KoinApplication.installModules(provider: KoinModuleProvider) {
    val modules = provider.provide()
    modules(modules.toList())
}