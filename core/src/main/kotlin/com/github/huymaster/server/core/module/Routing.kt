package com.github.huymaster.server.core.module

import com.github.huymaster.server.api.exceptions.ServiceException
import com.github.huymaster.server.core.net.*
import com.github.huymaster.server.core.utils.CircuitBreaker
import com.github.huymaster.server.core.utils.I18nPlugin
import com.github.huymaster.server.core.utils.LocaleContext.Companion.text
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.SQLException

fun Application.configureRouting() {
    install(Compression) {
        minimumSize(1024)
        condition {
            val isRangeRequest = this.response.headers.contains(HttpHeaders.AcceptRanges)
            val isDownload = this.response.headers.contains(HttpHeaders.ContentDisposition)
            val isIdentity = this.response.headers[HttpHeaders.ContentEncoding]?.lowercase() == "identity"

            !isRangeRequest && !isDownload && !isIdentity
        }
        gzip {
            priority = 1.0
            matchContentType(ContentType.Text.Any)
        }
        deflate {
            priority = 0.9
            matchContentType(ContentType.Text.Any)
        }
    }
    install(I18nPlugin)
    install(StatusPages) {
        exception<ServiceException> { call, cause ->
            val raw = cause.message?.takeIf { it.isNotBlank() }
            val message = raw?.let { call.text(it) } ?: call.text("error.unknown")
            call.respond(
                HttpStatusCode.fromValue(cause.code),
                message
            )
        }
        exception<BadRequestException> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.BadRequest,
                call.text("error.request_invalid")
            )
        }
        exception<CircuitBreaker.CircuitBreakerException> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                call.text("error.service_unavailable")
            )
        }
        exception<SQLException> { call, cause ->
            cause.printStackTrace()
            if (cause.message?.contains("closed") == true)
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    call.text("error.database_unavailable")
                )
            else
                call.respond(
                    HttpStatusCode.InternalServerError,
                    call.text("error.database_error")
                )
        }
        status(HttpStatusCode.Unauthorized) {
            call.respond(
                HttpStatusCode.Unauthorized,
                call.text("error.unauthorized")
            )
        }
        exception<ContentTransformationException> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.BadRequest,
                call.text("error.request_invalid")
            )
        }
        exception<NotImplementedError> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.NotImplemented,
                call.text("error.not_implemented")
            )
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                call.text("error.internal")
            )
        }
    }
    install(AutoHeadResponse)
    install(Resources)
    routing {
        BaseRoutes.register(this)
        AuthRoutes.register(this)
        FileRoutes.register(this)
        UserRoutes.register(this)
        KeyRoutes.register(this)
    }
}