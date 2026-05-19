package com.github.huymaster.server.api.security

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import javax.crypto.SecretKey

object AESSymmetricCipher : SymmetricCipher<ByteArray, ByteArray, SecretKey> {
    private const val GCM_MAC_SIZE_BITS = 128

    override fun encrypt(
        plaintext: ByteArray,
        key: SecretKey,
        iv: ByteArray
    ): ByteArray {
        require(iv.size == 12) { "IV must be 12 bytes" }
        val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
        val parameters = AEADParameters(
            KeyParameter(key.encoded),
            GCM_MAC_SIZE_BITS,
            iv
        )

        cipher.init(true, parameters)
        val size = cipher.getOutputSize(plaintext.size)
        val output = ByteArray(size)
        val processed = cipher.processBytes(plaintext, 0, plaintext.size, output, 0)
        cipher.doFinal(output, processed)
        return output
    }

    override fun decrypt(
        ciphertext: ByteArray,
        key: SecretKey,
        iv: ByteArray
    ): ByteArray {
        require(iv.size == 12) { "IV must be 12 bytes" }
        val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
        val parameters = AEADParameters(
            KeyParameter(key.encoded),
            GCM_MAC_SIZE_BITS,
            iv
        )

        cipher.init(false, parameters)
        val size = cipher.getOutputSize(ciphertext.size)
        val output = ByteArray(size)
        val processed = cipher.processBytes(ciphertext, 0, ciphertext.size, output, 0)
        cipher.doFinal(output, processed)
        return output
    }
}