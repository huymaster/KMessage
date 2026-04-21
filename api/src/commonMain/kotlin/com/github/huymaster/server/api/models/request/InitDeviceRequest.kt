package com.github.huymaster.server.api.models.request

import kotlinx.serialization.Serializable

@Serializable
data class InitDeviceRequest(
    val deviceId: String,
    val identityKey: ByteArray,
    val deviceName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InitDeviceRequest

        if (deviceId != other.deviceId) return false
        if (!identityKey.contentEquals(other.identityKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + identityKey.contentHashCode()
        return result
    }
}