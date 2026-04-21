package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.core.service.KeyService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject

object BaseRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.BASE_SERVICE
    val keyService by inject<KeyService>()

    override fun Route.registerRoutes() {
        get(Endpoints.BASE_SERVICE_HEALTH) { call.respond(HttpStatusCode.OK) }

        get {
            val cleaned = keyService.cleanUpSignedKeys()
            call.respond("Cleaned up $cleaned signed keys")
        }
    }
}