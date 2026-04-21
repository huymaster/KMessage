package com.github.huymaster.server.core.net

import io.ktor.server.routing.*
import org.koin.core.component.KoinComponent

abstract class BaseRoute : KoinComponent {
    fun register(route: Route) =
        route.route(endpoint) { registerRoutes() }

    abstract val endpoint: String
    protected abstract fun Route.registerRoutes()
}