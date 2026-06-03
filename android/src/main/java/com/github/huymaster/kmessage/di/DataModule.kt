package com.github.huymaster.kmessage.di

import com.github.huymaster.kmessage.core.data.repository.AuthRepository
import com.github.huymaster.kmessage.core.data.repository.AuthRepositoryImpl
import com.github.huymaster.kmessage.core.data.source.AuthRemoteDataSource
import com.github.huymaster.kmessage.core.data.source.AuthRemoteDataSourceImpl
import com.github.huymaster.kmessage.core.data.source.AuthTokenDataSource
import com.github.huymaster.kmessage.core.data.source.AuthTokenDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

object DataModule : KoinModuleProvider {
    private val module = module {
        single<AuthTokenDataSource> { AuthTokenDataSourceImpl(androidContext()) }
        single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
        single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    }

    override fun provide(): Set<Module> = setOf(module)
}