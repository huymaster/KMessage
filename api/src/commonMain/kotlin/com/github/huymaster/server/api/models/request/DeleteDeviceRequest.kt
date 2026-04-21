package com.github.huymaster.server.api.models.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteDeviceRequest(
    val deviceId: String
)