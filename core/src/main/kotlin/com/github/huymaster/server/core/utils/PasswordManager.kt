package com.github.huymaster.server.core.utils

import com.github.huymaster.server.api.security.Argon2KeyDerivation
import com.github.huymaster.server.api.utils.getRandomBytes
import com.github.huymaster.server.api.utils.mergeSalt
import com.github.huymaster.server.api.utils.splitSalt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

object PasswordManager {
    private val keyDeriver = Argon2KeyDerivation.DEFAULT
    private val charset = Charsets.UTF_8

    private const val SALT_LENGTH = 16

    fun hash(password: String): ByteArray {
        val passwordBytes = password.toByteArray(charset)
        val salt = getRandomBytes(SALT_LENGTH)

        try {
            val hash = keyDeriver.deriveKey(passwordBytes, salt)
            return mergeSalt(salt, hash)
        } finally {
            passwordBytes.fill(0)
        }
    }

    fun verify(password: String, combined: ByteArray): Boolean {
        if (combined.size <= SALT_LENGTH) return false

        val (salt, targetHash) = splitSalt(combined)
        val passwordBytes = password.toByteArray(charset)
        try {
            val computedHash = keyDeriver.deriveKey(passwordBytes, salt)
            return MessageDigest.isEqual(computedHash, targetHash)
        } finally {
            passwordBytes.fill(0)
        }
    }

    suspend fun hashSuspend(password: String): ByteArray =
        withContext(Dispatchers.IO) { hash(password) }

    suspend fun verifySuspend(password: String, hash: ByteArray): Boolean =
        withContext(Dispatchers.IO) { verify(password, hash) }
}