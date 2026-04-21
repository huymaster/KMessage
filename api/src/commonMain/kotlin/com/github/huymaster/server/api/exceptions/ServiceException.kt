package com.github.huymaster.server.api.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class ServiceException(
    val code: Int,
    override val message: String? = null
) : Exception(message) {
    companion object {
        fun error(code: Int, message: String? = null): Nothing = throw ServiceException(code, message)
    }
}