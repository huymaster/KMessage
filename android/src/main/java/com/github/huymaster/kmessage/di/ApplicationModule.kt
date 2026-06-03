package com.github.huymaster.kmessage.di

object ApplicationModule : KoinModuleProvider {
    private val modules = listOf(
        DataModule,
        NetworkModule
    )

    override fun provide() =
        modules.flatMap { it.provide() }.distinct().toSet()
}