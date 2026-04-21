package com.github.huymaster.server.core.utils

import com.github.huymaster.server.api.utils.buildJson
import kotlinx.serialization.modules.SerializersModule

private val module = SerializersModule {
}

val serverSideJson = buildJson {
    serializersModule = module
}
