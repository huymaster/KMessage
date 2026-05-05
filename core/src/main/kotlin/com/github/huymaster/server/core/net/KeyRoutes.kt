package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.core.service.KeyService
import io.ktor.server.routing.*
import org.koin.core.component.inject

object KeyRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.KEY_SERVICE

    private val keyService by inject<KeyService>()

    override fun Route.registerRoutes() {

    }
}