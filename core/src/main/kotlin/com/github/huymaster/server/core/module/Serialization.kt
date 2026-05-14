@file:OptIn(ExperimentalSerializationApi::class)

package com.github.huymaster.server.core.module

import com.github.huymaster.server.api.utils.DefaultCbor
import com.github.huymaster.server.api.utils.DefaultJson
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(DefaultJson)
        cbor(DefaultCbor)
    }
}