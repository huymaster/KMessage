package com.github.huymaster.server.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class InitDeviceDto(
    val userId: String,
    val deviceId: String,
    val identityKey: ByteArray,
    val deviceName: String? = null
) : BaseDto<Nothing> {
    override fun toEntity(): Nothing = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InitDeviceDto

        if (userId != other.userId) return false
        if (deviceId != other.deviceId) return false
        if (!identityKey.contentEquals(other.identityKey)) return false
        if (deviceName != other.deviceName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + deviceId.hashCode()
        result = 31 * result + identityKey.contentHashCode()
        result = 31 * result + (deviceName?.hashCode() ?: 0)
        return result
    }
}