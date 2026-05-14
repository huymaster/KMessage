package com.github.huymaster.server.api.models.request

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegisterRequest(
    @Contextual val mlkemPublicKey: ByteArray,
    @Contextual val edPublicKey: ByteArray,
    val deviceName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceRegisterRequest) return false

        if (!mlkemPublicKey.contentEquals(other.mlkemPublicKey)) return false
        if (!edPublicKey.contentEquals(other.edPublicKey)) return false
        if (deviceName != other.deviceName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mlkemPublicKey.contentHashCode()
        result = 31 * result + edPublicKey.contentHashCode()
        result = 31 * result + (deviceName?.hashCode() ?: 0)
        return result
    }
}