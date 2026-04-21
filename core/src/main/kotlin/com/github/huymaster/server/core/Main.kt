package com.github.huymaster.server.core

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import com.github.huymaster.server.core.di.Modules
import com.github.huymaster.server.core.module.configure
import com.github.huymaster.server.core.utils.EnvironmentVariables
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private fun configureConsoleLogger() {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    context.reset()

    val encoder = PatternLayoutEncoder().apply {
        this.context = context
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        start()
    }

    val consoleAppender = ConsoleAppender<ILoggingEvent>().apply {
        this.context = context
        this.name = "console"
        this.encoder = encoder
        start()
    }

    val root = context.getLogger(Logger.ROOT_LOGGER_NAME)
    root.level = if (EnvironmentVariables.DEBUG.VALUE) Level.TRACE else Level.INFO
    root.addAppender(consoleAppender)
}

private fun configureKoin() {
    startKoin {
        slf4jLogger()
        modules(Modules.modules())
    }
}

fun main(args: Array<String>) {
    configureConsoleLogger()
    configureKoin()
    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::configure
    ).start(true)
}