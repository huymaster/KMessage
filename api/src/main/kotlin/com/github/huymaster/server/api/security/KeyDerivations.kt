package com.github.huymaster.server.api.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.nio.Buffer
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

abstract class AbstractKeyDerivation<P, S> : KeyDerivation<P, S> {
    protected fun clearBuffer(vararg buffers: Buffer) {
        buffers.forEach { buffer -> buffer.clear() }
    }

    protected fun clearArray(vararg arrays: ByteArray) =
        arrays.forEach { array -> array.fill(0) }

    protected fun clearArray(vararg arrays: CharArray) =
        arrays.forEach { array -> array.fill('\u0000') }
}

class PBKDF2KeyDerivation(
    private val iterationCount: Int,
    private val keyLengthBits: Int
) : AbstractKeyDerivation<CharArray, SecretKey>() {
    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        val DEFAULT = PBKDF2KeyDerivation(600000, 256)
    }

    private val factory: SecretKeyFactory get() = SecretKeyFactory.getInstance(ALGORITHM)

    init {
        require(iterationCount >= 100000) { "Iteration count must be greater than 100000" }
        require(keyLengthBits > 0) { "Key length must be greater than 0" }
    }

    override fun deriveKey(password: CharArray, salt: ByteArray): SecretKey {
        require(password.isNotEmpty()) { "Password should not be empty" }
        require(salt.isNotEmpty()) { "Salt should not be empty" }

        val spec = PBEKeySpec(password, salt, iterationCount, keyLengthBits)
        return try {
            factory.generateSecret(spec)
        } catch (e: Exception) {
            throw RuntimeException("Failed to derive key via PBKDF2", e)
        } finally {
            spec.clearPassword()
        }
    }
}

class Argon2KeyDerivation(
    private val type: Int,
    private val iterationCount: Int,
    private val parallelism: Int,
    private val memoryKB: Int,
    private val outputLengthBytes: Int = 32
) : AbstractKeyDerivation<ByteArray, ByteArray>() {
    companion object {
        val DEFAULT = Argon2KeyDerivation(Argon2Parameters.ARGON2_id, 3, 4, 32768)
    }

    init {
        require(iterationCount > 0) { "Iteration count must be > 0" }
        require(parallelism > 0) { "Parallelism must be > 0" }
        require(memoryKB > 0) { "Memory KB must be > 0" }
    }

    override fun deriveKey(password: ByteArray, salt: ByteArray): ByteArray {
        require(password.isNotEmpty()) { "Password should not be empty" }
        require(salt.isNotEmpty()) { "Salt should not be empty" }

        try {
            val generator = Argon2BytesGenerator()
            val params = Argon2Parameters.Builder(type)
                .withIterations(iterationCount)
                .withParallelism(parallelism)
                .withMemoryAsKB(memoryKB)
                .withSalt(salt)
                .build()
            val out = ByteArray(outputLengthBytes)
            generator.apply { init(params) }
                .generateBytes(password, out)
            return out
        } catch (e: Exception) {
            throw RuntimeException("Failed to derive key", e)
        }
    }
}