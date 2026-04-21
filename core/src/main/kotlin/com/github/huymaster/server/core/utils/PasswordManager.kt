package com.github.huymaster.server.core.utils

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Contract
import java.util.*

object PasswordManager {
    private const val ITERATION = 3
    private const val MEMORY = 32768
    private const val PARALLELISM = 4

    private val charset = Charsets.UTF_8
    private val encoder: Base64.Encoder = Base64.getEncoder()
    private val decoder: Base64.Decoder = Base64.getDecoder()
    private val argon2: Argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    @Contract(pure = true)
    fun hash(password: String): ByteArray {
        val hash = argon2.hash(ITERATION, MEMORY, PARALLELISM, password.toCharArray(), charset)
        return encoder.encode(hash.toByteArray(charset))
    }

    @Contract(pure = true)
    fun verify(password: String, hash: ByteArray): Boolean {
        val hashString = decoder.decode(hash).toString(charset)
        return argon2.verify(hashString, password.toCharArray(), charset)
    }

    @Contract(pure = true)
    suspend fun hashSuspend(password: String): ByteArray =
        withContext(Dispatchers.IO) { hash(password) }

    @Contract(pure = true)
    suspend fun verifySuspend(password: String, hash: ByteArray): Boolean =
        withContext(Dispatchers.IO) { verify(password, hash) }
}