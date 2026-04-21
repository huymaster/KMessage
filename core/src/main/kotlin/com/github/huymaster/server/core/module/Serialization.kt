@file:OptIn(ExperimentalSerializationApi::class)

package com.github.huymaster.server.core.module

import com.github.huymaster.server.core.utils.serverSideJson
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(serverSideJson)
    }
}