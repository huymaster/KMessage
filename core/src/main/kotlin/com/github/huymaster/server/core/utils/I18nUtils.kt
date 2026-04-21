package com.github.huymaster.server.core.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.jetbrains.annotations.PropertyKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LocaleContext private constructor(val locale: Locale = Locale.ENGLISH) {
    companion object {
        private const val BUNDLE_KEY = "messages.messages"
        private val instances = ConcurrentHashMap<Locale, LocaleContext>()
        private val logger: Logger = LoggerFactory.getLogger(LocaleContext::class.java)
        val DEFAULT = LocaleContext()
        val RoutingContext.localeContext: LocaleContext get() = call.localeContext
        val ApplicationCall.localeContext: LocaleContext get() = getOrCreate(locale)

        fun getOrCreate(locale: Locale): LocaleContext = instances.getOrPut(locale) { LocaleContext(locale) }

        suspend fun RoutingContext.text(
            @PropertyKey(resourceBundle = BUNDLE_KEY) key: String
        ): String = localeContext.text(key)

        suspend fun RoutingContext.text(
            @PropertyKey(resourceBundle = BUNDLE_KEY) key: String,
            vararg args: Any?
        ): String = localeContext.text(key, *args)

        suspend fun ApplicationCall.text(
            @PropertyKey(resourceBundle = BUNDLE_KEY) key: String
        ): String = localeContext.text(key)

        suspend fun ApplicationCall.text(
            @PropertyKey(resourceBundle = BUNDLE_KEY) key: String,
            vararg args: Any?
        ): String = localeContext.text(key, *args)
    }

    private val bundle: ResourceBundle = ResourceBundle.getBundle(BUNDLE_KEY, locale)

    fun isAvailable(): Boolean =
        bundle.keySet().size == DEFAULT.bundle.keySet().size

    fun text(
        @PropertyKey(resourceBundle = BUNDLE_KEY) key: String
    ): String = runCatching { bundle.getString(key) }
        .onFailure { logger.warn("Failed to get text for key $key. Failback to default.", it) }
        .getOrElse {
            if (this == DEFAULT && !DEFAULT.contains(key)) key
            else DEFAULT.text(key)
        }

    fun text(
        @PropertyKey(resourceBundle = BUNDLE_KEY) key: String,
        vararg args: Any?
    ): String = runCatching {
        val pattern = text(key)
        val format = MessageFormat(pattern, locale)
        format.format(args)
    }.getOrElse { text(key) }

    operator fun get(
        @PropertyKey(resourceBundle = BUNDLE_KEY) key: String
    ) = text(key)

    operator fun get(
        @PropertyKey(resourceBundle = BUNDLE_KEY) key: String,
        vararg args: Any?
    ) = text(key, *args)

    fun contains(key: String) = bundle.containsKey(key)
}

private val LOCALE = AttributeKey<Locale>("DesiredLocale")

val I18nPlugin = createApplicationPlugin("I18nPlugin") {
    onCall {
        val desireLanguage = it.request.acceptLanguageItems()
        val locale =
            Locale.forLanguageTag(desireLanguage.firstOrNull()?.value ?: Locale.ENGLISH.toLanguageTag())

        it.attributes[LOCALE] = locale
    }

    onCallRespond { call ->
        runCatching { call.attributes[LOCALE] }
            .onSuccess { call.response.header(HttpHeaders.ContentLanguage, it.toLanguageTag()) }
    }
}

val ApplicationCall.locale: Locale
    get() = runCatching { attributes[LOCALE] }.getOrNull() ?: Locale.ENGLISH