package com.github.huymaster.server.core.module

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.common.TokenCookie
import com.github.huymaster.server.core.utils.EnvironmentVariables
import com.github.huymaster.server.core.utils.JWTManager
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.Security
import kotlin.time.Duration.Companion.milliseconds

const val APP_DOMAIN = "base"
const val UNSAFE_VARIABLE = "unsafe"

fun Application.configureSecurity() {
    Security.addProvider(BouncyCastleProvider())

    install(CORS) {
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowSameOrigin = true
        allowHeaders { true }
        allowOrigins { true }
        anyHost()
        anyMethod()
    }
    install(CallLogging) {
        format { call ->
            val request = call.request
            val response = call.response
            val origin = request.origin

            val hostDisplay = origin.remoteHost.takeIf { it.isNotBlank() && it != origin.remoteAddress }
                ?.let { "$it (${origin.remoteAddress})" }
                ?: origin.remoteAddress

            val processingTime = call.processingTimeMillis()
            "[${hostDisplay}:${origin.remotePort}] ${response.status()}: ${request.httpMethod.value} - ${request.uri} in $processingTime ms"
        }
    }
    install(ForwardedHeaders)
    install(XForwardedHeaders)
    install(IgnoreTrailingSlash)
    if (!EnvironmentVariables.DEBUG.VALUE) {
        install(HSTS)
        install(HttpsRedirect) {
            sslPort = 443
            permanentRedirect = false
        }
    }
    install(Authentication) {
        jwt(APP_DOMAIN) {
            verifier(JWTManager.getVerifier())
            validate { credential ->
                if (JWTManager.validateJWT(credential))
                    JWTManager.extractToken(JWTPrincipal(credential.payload))
                else
                    null
            }
        }
    }
    install(Sessions) {
        cookie<TokenCookie>("${APP_DOMAIN}_session", directorySessionStorage(File("_sessions_"))) {
            cookie.path = "/${Endpoints.AUTH_SERVICE}"
            cookie.extensions["SameSite"] = "lax"
            cookie.httpOnly = true
            cookie.secure = !EnvironmentVariables.DEBUG.VALUE
            cookie.maxAge = JWTManager.REFRESH_TOKEN_EXPIRATION.milliseconds
        }
    }
}