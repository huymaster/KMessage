package com.github.huymaster.server.api.security

import com.github.huymaster.server.api.utils.getSecureRandom
import org.bouncycastle.crypto.CipherKeyGenerator
import org.bouncycastle.crypto.KeyGenerationParameters
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AESKeyGenerator private constructor(
    private val strong: Boolean
) : KeyGenerator<SecretKey> {

    companion object {
        private const val KEY_SIZE_BITS = 256

        private val instances = ConcurrentHashMap<Boolean, AESKeyGenerator>()

        fun getInstance(strong: Boolean = false): AESKeyGenerator =
            instances.getOrPut(strong) { AESKeyGenerator(strong) }
    }

    override fun generate(): SecretKey {
        val keyGenerator = CipherKeyGenerator()
        val random = getSecureRandom(strong)
        val param = KeyGenerationParameters(random, KEY_SIZE_BITS)
        keyGenerator.init(param)
        val rawKeyBytes = keyGenerator.generateKey()
        return SecretKeySpec(rawKeyBytes, "AES")
    }
}