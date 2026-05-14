package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.common.ServerInfo
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object BaseRoutes : BaseRoute() {
    override val endpoint: String
        get() = ""

    override fun Route.registerRoutes() {
        get { call.respondRedirect("https://example.com") }
        get(Endpoints.BASE_SERVICE_HEALTH) { call.respond(HttpStatusCode.OK) }
        get(Endpoints.BASE_SERVICE_INFO) {
            val runtime = Runtime.getRuntime()
            val free = runtime.freeMemory()
            val total = runtime.totalMemory()
            val info = ServerInfo(
                usedMemory = total - free,
                freeMemory = runtime.freeMemory(),
                totalMemory = runtime.totalMemory(),
                maxMemory = runtime.maxMemory()
            )
            call.respond(info)
        }
    }

}