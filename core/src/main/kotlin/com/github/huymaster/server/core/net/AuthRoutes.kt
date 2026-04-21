package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.common.TokenCookie
import com.github.huymaster.server.api.models.request.LoginRequest
import com.github.huymaster.server.api.models.request.RegisterRequest
import com.github.huymaster.server.core.service.AuthService
import com.github.huymaster.server.core.utils.LocaleContext.Companion.localeContext
import com.github.huymaster.server.core.utils.requestOrNull
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.core.component.inject

object AuthRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.AUTH_SERVICE

    private val authService by inject<AuthService>()

    override fun Route.registerRoutes() {
        post(Endpoints.AUTH_SERVICE_REGISTER) {
            val request = call.requestOrNull<RegisterRequest>()
                ?: serviceException(HttpStatusCode.BadRequest, "error.request_invalid")

            authService.register(request.username, request.password).getOrThrow()
            call.respond(HttpStatusCode.Created, localeContext["register.success"])
        }

        post(Endpoints.AUTH_SERVICE_LOGIN) {
            val cookie = call.sessions.get<TokenCookie>()
            if (cookie != null) {
                val verifyResult = authService.verifyRefreshToken(cookie.token)
                    .getOrElse { false }
                if (!verifyResult) call.sessions.clear<TokenCookie>()
                else serviceException(HttpStatusCode.Forbidden, "login.already_login")
            }

            val request = call.requestOrNull<LoginRequest>()
                ?: serviceException(HttpStatusCode.BadRequest, "error.request_invalid")

            val ipAddress = call.request.origin.remoteAddress
            val token = authService.login(request.username, request.password, ipAddress).getOrThrow()
            call.sessions.set(TokenCookie(token))
            call.respond(HttpStatusCode.OK, localeContext["login.success"])
        }

        get(Endpoints.AUTH_SERVICE_REFRESH) {
            val cookie = call.sessions.get<TokenCookie>()
                ?: serviceException(HttpStatusCode.Unauthorized, "refresh.refresh_token_not_found")

            val ipAddress = call.request.origin.remoteAddress
            val pair = authService.refreshToken(cookie.token, ipAddress).getOrThrow()
            call.sessions.set(TokenCookie(pair.first))
            call.respond(HttpStatusCode.OK, pair.second)
        }

        delete(Endpoints.AUTH_SERVICE_LOGOUT) {
            val token = call.sessions.get<TokenCookie>()
                ?: serviceException(HttpStatusCode.Unauthorized, "logout.failed")

            authService.logout(token.token).getOrThrow()

            call.sessions.clear<TokenCookie>()
            call.respond(HttpStatusCode.OK, localeContext["logout.success"])
        }
    }
}