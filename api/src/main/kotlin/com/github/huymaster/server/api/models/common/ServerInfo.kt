package com.github.huymaster.server.api.models.common

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val usedMemory: Long,
    val freeMemory: Long,
    val totalMemory: Long,
    val maxMemory: Long,
    val timestamp: Long = System.currentTimeMillis()
)