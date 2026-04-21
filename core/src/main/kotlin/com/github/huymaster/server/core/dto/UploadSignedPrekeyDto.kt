package com.github.huymaster.server.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadSignedPrekeyDto(
    val deviceId: String,
    val signedPrekey: ByteArray,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadSignedPrekeyDto) return false

        if (deviceId != other.deviceId) return false
        if (!signedPrekey.contentEquals(other.signedPrekey)) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + signedPrekey.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}