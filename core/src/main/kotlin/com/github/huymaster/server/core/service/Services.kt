package com.github.huymaster.server.core.service

import org.koin.dsl.module

object Services {
    val ALL = module {
        single { AuthService() }
        single { UserService() }
        single { FileService() }
        single { KeyService() }
        single { RedisService(get()) }
        factory { MinioService(get()) }
    }
}