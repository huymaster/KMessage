package com.github.huymaster.server.api.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.SerializersModule

private val defaultSerializersModule = SerializersModule {
}

val DefaultJson = Json {
    serializersModule = defaultSerializersModule
    allowTrailingComma = true
    classDiscriminator = "__type__"
    coerceInputValues = true
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

fun buildJson(from: Json = DefaultJson, builder: JsonBuilder.() -> Unit) = Json(from) { builder() }