package com.github.huymaster.kmessage.di

import org.koin.core.component.KoinComponent
import org.koin.core.module.Module

interface KoinModuleProvider : KoinComponent {
    fun provide(): Set<Module>
}