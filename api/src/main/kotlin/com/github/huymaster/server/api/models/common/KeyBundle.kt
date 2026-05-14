package com.github.huymaster.server.api.models.common

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class KeyBundle(
    val mlkemKeys: List<@Contextual ByteArray>,
    val edKeys: List<@Contextual ByteArray>
)