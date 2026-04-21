package com.github.huymaster.server.core.utils

import com.github.huymaster.server.api.exceptions.ServiceException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.util.*

fun RoutingContext.getUserId(): String? = call.principal<JWTManager.Token.Access>()?.userId

fun RoutingContext.getUserIdAsUUID(): UUID? =
    getUserId()?.let { runCatching { UUID.fromString(it) }.getOrNull() }

fun RoutingContext.getIpAddress(): String = call.request.origin.remoteHost

suspend inline fun <reified T> ApplicationCall.requestOrNull(): T? =
    runCatching { receiveNullable<T>() }.getOrNull()

suspend inline fun <reified T> ApplicationCall.requestOrThrow(
    exception: Exception = ServiceException(HttpStatusCode.BadRequest.value, "error.request_invalid")
): T = requestOrNull() ?: throw exception

suspend inline fun <reified T> ApplicationCall.requestOrThrow(`throw`: () -> Nothing): T =
    requestOrNull() ?: `throw`()

fun parseRangeHeader(header: String?): LongRange? = runCatching {
    if (header == null || !header.startsWith("bytes=", true)) return null

    val content = header.substringAfter("bytes=").trim()
    if (content.isEmpty()) return null

    val parts = content.split(",").map(String::trim).filter { it.isNotEmpty() }
    if (parts.size != 1) return null

    val part = parts.first()

    return when {
        part.startsWith("-") -> {
            val end = part.substringAfter("-").toLong()
            Long.MIN_VALUE..end
        }

        part.endsWith("-") -> {
            val start = part.substringBefore("-").toLong()
            start..Long.MAX_VALUE
        }

        part.contains("-") -> {
            val (start, end) = part.split("-").map(String::toLong)
            start..end
        }

        else -> null
    }
}.getOrNull()