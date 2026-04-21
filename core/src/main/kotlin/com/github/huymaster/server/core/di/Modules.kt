package com.github.huymaster.server.core.di

import com.github.huymaster.server.core.service.Services

object Modules {
    fun modules() = listOf(
        Database.PSQL, Database.REDIS, Database.MINIO,
        Services.ALL
    )
}