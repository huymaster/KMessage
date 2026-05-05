package com.github.huymaster.server.core.net

import io.ktor.server.routing.*
import org.koin.core.component.KoinComponent

abstract class BaseRoute : KoinComponent {
    companion object {
        val routes = listOf(
            AuthRoutes,
            BaseRoutes,
            FileRoutes,
            KeyRoutes,
            UserRoutes,
        )
    }

    fun register(route: Route) =
        route.route("") { registerRoutes() }

    abstract val endpoint: String
    protected abstract fun Route.registerRoutes()
}