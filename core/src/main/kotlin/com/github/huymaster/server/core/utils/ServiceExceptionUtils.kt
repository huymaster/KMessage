package com.github.huymaster.server.core.utils

import com.github.huymaster.server.api.exceptions.ServiceException
import io.ktor.http.*
import org.jetbrains.annotations.PropertyKey

fun serviceException(code: HttpStatusCode): Nothing =
    serviceException(code.value)

fun serviceException(
    code: HttpStatusCode,
    @PropertyKey(resourceBundle = "messages.messages")
    message: String
): Nothing = serviceException(code.value, message)

fun serviceException(code: Int): Nothing = throw ServiceException(code)

fun serviceException(
    code: Int,
    @PropertyKey(resourceBundle = "messages.messages")
    message: String
): Nothing =
    throw ServiceException(code, message)