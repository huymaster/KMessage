package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.request.LoginRequest
import com.github.huymaster.server.api.models.request.RegisterRequest
import com.github.huymaster.server.core.module.APP_DOMAIN
import com.github.huymaster.server.core.service.AuthService
import com.github.huymaster.server.core.utils.JWTManager
import com.github.huymaster.server.core.utils.LocaleContext.Companion.localeContext
import com.github.huymaster.server.core.utils.getUserId
import com.github.huymaster.server.core.utils.requestOrNull
import com.github.huymaster.server.core.utils.requestOrThrow
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.auth.*
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

        authenticate(APP_DOMAIN, optional = true) {
            post(Endpoints.AUTH_SERVICE_LOGIN) {
                if (getUserId() != null)
                    serviceException(HttpStatusCode.Forbidden, "login.already_login")

                val request = call.requestOrNull<LoginRequest>()
                    ?: serviceException(HttpStatusCode.BadRequest, "error.request_invalid")

                val ipAddress = call.request.origin.remoteAddress
                val token = authService.login(request.username, request.password, ipAddress).getOrThrow()
                call.respond(HttpStatusCode.OK, token)
            }
        }

        get(Endpoints.AUTH_SERVICE_REFRESH) {
            val refreshToken = call.requestOrThrow<String>()
            val ipAddress = call.request.origin.remoteAddress
            val pair = authService.refreshToken(refreshToken, ipAddress).getOrThrow()
            call.respond(HttpStatusCode.OK, pair)
        }

        authenticate(APP_DOMAIN) {
            delete(Endpoints.AUTH_SERVICE_LOGOUT) {
                val accessToken = call.principal<JWTManager.Token.Access>()
                    ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                authService.logout(accessToken.createdBy).getOrThrow()
                call.respond(HttpStatusCode.OK, localeContext["logout.success"])
            }
        }
    }
}