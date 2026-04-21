package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.core.module.APP_DOMAIN
import com.github.huymaster.server.core.service.UserService
import com.github.huymaster.server.core.utils.getUserId
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject

object UserRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.USER_SERVICE

    private val userService by inject<UserService>()

    override fun Route.registerRoutes() {
        authenticate(APP_DOMAIN) {
            get(Endpoints.USER_SERVICE_USER_ID) {
                val userId = getUserId() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                call.respond(userId)
            }
        }
    }
}