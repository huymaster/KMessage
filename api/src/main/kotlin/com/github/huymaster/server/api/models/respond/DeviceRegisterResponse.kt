package com.github.huymaster.server.api.models.respond

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegisterResponse(
    val registrationId: Int
)