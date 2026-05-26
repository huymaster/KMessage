package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.request.DeviceRegisterRequest
import com.github.huymaster.server.api.models.respond.DeviceRegisterResponse
import com.github.huymaster.server.core.module.APP_DOMAIN
import com.github.huymaster.server.core.service.KeyService
import com.github.huymaster.server.core.utils.getUserIdAsUUID
import com.github.huymaster.server.core.utils.requestOrThrow
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject
import java.util.*

object KeyRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.KEY_SERVICE

    private val keyService by inject<KeyService>()

    override fun Route.registerRoutes() {
        authenticate(APP_DOMAIN) {
            post(Endpoints.KEY_SERVICE_REGISTER) {
                val userId = getUserIdAsUUID() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                val request = call.requestOrThrow<DeviceRegisterRequest>()
                val registrationId = keyService.registerDevice(
                    userId,
                    request.mlkemPublicKey,
                    request.mldsaPublicKey,
                    request.edPublicKey,
                    request.signature,
                    request.deviceName
                ).getOrThrow()
                call.respond(DeviceRegisterResponse(registrationId))
            }
        }
        get(Endpoints.KEY_SERVICE_GET_BUNDLE) {
            val userId = runCatching {
                val string = call.request.queryParameters["userId"]
                UUID.fromString(string)
            }.getOrNull() ?: serviceException(HttpStatusCode.BadRequest, "key.invalid_user")

            val bundle = keyService.getKeyBundle(userId)
                .getOrThrow()
            call.respond(bundle)
        }
    }
}