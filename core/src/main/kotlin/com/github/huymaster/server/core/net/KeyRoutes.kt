package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.request.DeleteDeviceRequest
import com.github.huymaster.server.api.models.request.InitDeviceRequest
import com.github.huymaster.server.core.dto.DeleteDeviceDto
import com.github.huymaster.server.core.dto.InitDeviceDto
import com.github.huymaster.server.core.module.APP_DOMAIN
import com.github.huymaster.server.core.service.KeyService
import com.github.huymaster.server.core.utils.getUserId
import com.github.huymaster.server.core.utils.requestOrThrow
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject

object KeyRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.KEY_SERVICE

    private val keyService by inject<KeyService>()

    override fun Route.registerRoutes() {
        authenticate(APP_DOMAIN) {
            post(Endpoints.KEY_SERVICE_INIT_DEVICE) {
                initDevice()
            }
            delete(Endpoints.KEY_SERVICE_DELETE_DEVICE) {
                deleteDevice()
            }
            post(Endpoints.KEY_SERVICE_UPLOAD_SIGNED_PREKEYS) {

            }
        }
    }

    private suspend fun RoutingContext.initDevice() {
        val userId = getUserId() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
        val request = call.requestOrThrow<InitDeviceRequest>()

        val dto = InitDeviceDto(
            userId = userId,
            deviceId = request.deviceId,
            identityKey = request.identityKey,
            deviceName = request.deviceName
        )
        val registrationId = keyService.initDevice(dto)
            .getOrThrow()
        call.respond(registrationId)
    }

    private suspend fun RoutingContext.deleteDevice() {
        val userId = getUserId() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
        val request = call.requestOrThrow<DeleteDeviceRequest>()

        keyService.deleteDevice(DeleteDeviceDto(userId, request.deviceId)).getOrThrow()
        call.respond(HttpStatusCode.OK)
    }
}