package com.github.huymaster.server.core.module

import io.ktor.server.application.*

fun Application.configure() {
    configureFrameworks()
    configureSecurity()
    configureSerialization()
    configureRouting()
}