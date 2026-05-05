package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object BaseRoutes : BaseRoute() {
    override val endpoint: String
        get() = ""


    override fun Route.registerRoutes() {
        get {
            call.respondRedirect("https://example.com")
        }
        get(Endpoints.BASE_SERVICE_HEALTH) { call.respond(HttpStatusCode.OK) }
    }
}