package com.github.huymaster.server.core.utils

import com.github.huymaster.server.core.module.UNSAFE_VARIABLE
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

sealed class Environment<out T>(open val key: String) : ReadOnlyProperty<Any?, T> {

    data class NotFound(override val key: String) : Environment<Nothing>(key) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Nothing {
            throw NoSuchElementException("Environment variable '$key' is not found.")
        }
    }

    sealed class ValueEnvironment<out T>(override val key: String, open val value: T) : Environment<T>(key) {
        data class StringValue(override val key: String, override val value: String) :
            ValueEnvironment<String>(key, value) {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
        }

        data class BooleanValue(override val key: String, override val value: Boolean) :
            ValueEnvironment<Boolean>(key, value) {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
        }

        data class IntValue(override val key: String, override val value: Int) :
            ValueEnvironment<Int>(key, value) {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
        }

        data class LongValue(override val key: String, override val value: Long) :
            ValueEnvironment<Long>(key, value) {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
        }

        data class DoubleValue(override val key: String, override val value: Double) :
            ValueEnvironment<Double>(key, value) {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
        }
    }

    val VALUE: T by this
}

@Suppress("unused")
object EnvironmentVariables {
    fun string(key: String, default: String? = null): Environment<String> {
        val value = System.getenv(key)
        return if (value != null) Environment.ValueEnvironment.StringValue(key, value)
        else if (default != null) Environment.ValueEnvironment.StringValue(key, default)
        else Environment.NotFound(key)
    }

    fun boolean(key: String, default: Boolean? = null): Environment<Boolean> {
        val value = System.getenv(key)?.toBooleanStrictOrNull()
        return if (value != null) Environment.ValueEnvironment.BooleanValue(key, value)
        else if (default != null) Environment.ValueEnvironment.BooleanValue(key, default)
        else Environment.NotFound(key)
    }

    fun int(key: String, default: Int? = null): Environment<Int> {
        val value = System.getenv(key)?.toIntOrNull()
        return if (value != null) Environment.ValueEnvironment.IntValue(key, value)
        else if (default != null) Environment.ValueEnvironment.IntValue(key, default)
        else Environment.NotFound(key)
    }

    fun long(key: String, default: Long? = null): Environment<Long> {
        val value = System.getenv(key)?.toLongOrNull()
        return if (value != null) Environment.ValueEnvironment.LongValue(key, value)
        else if (default != null) Environment.ValueEnvironment.LongValue(key, default)
        else Environment.NotFound(key)
    }

    fun double(key: String, default: Double? = null): Environment<Double> {
        val value = System.getenv(key)?.toDoubleOrNull()
        return if (value != null) Environment.ValueEnvironment.DoubleValue(key, value)
        else if (default != null) Environment.ValueEnvironment.DoubleValue(key, default)
        else Environment.NotFound(key)
    }

    val DEBUG = boolean("DEBUG", false)

    val HOST = string("HOST", "localhost")

    val JWT_REALM = string("JWT_REALM", UNSAFE_VARIABLE)
    val JWT_SECRET = string("JWT_SECRET", UNSAFE_VARIABLE)
    val JWT_AUDIENCE = string("JWT_AUDIENCE", UNSAFE_VARIABLE)

    val POSTGRES_HOST = string("POSTGRES_HOST")
    val POSTGRES_PORT = int("POSTGRES_PORT", 5432)
    val POSTGRES_DB = string("POSTGRES_DB")
    val POSTGRES_USER = string("POSTGRES_USER")
    val POSTGRES_PASSWORD = string("POSTGRES_PASSWORD")

    val REDIS_HOST = string("REDIS_HOST")
    val REDIS_PORT = int("REDIS_PORT", 6379)
    val REDIS_PASSWORD = string("REDIS_PASSWORD")

    val MINIO_HOST = string("MINIO_HOST")
    val MINIO_PORT = int("MINIO_PORT", 9000)
    val MINIO_USER = string("MINIO_USER")
    val MINIO_PASSWORD = string("MINIO_PASSWORD")
}