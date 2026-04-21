package com.github.huymaster.server.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteDeviceDto(
    val userId: String,
    val deviceId: String
) : BaseDto<Nothing> {
    override fun toEntity(): Nothing = throw UnsupportedOperationException()
}