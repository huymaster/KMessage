package com.github.huymaster.server.api.security

import com.github.huymaster.server.api.utils.getSecureRandom
import org.bouncycastle.crypto.kems.MLKEMExtractor
import org.bouncycastle.crypto.kems.MLKEMGenerator
import org.bouncycastle.crypto.params.MLKEMPrivateKeyParameters
import org.bouncycastle.crypto.params.MLKEMPublicKeyParameters
import java.util.concurrent.ConcurrentHashMap

class MLKYBERKeyEncapsulation private constructor(
    strong: Boolean
) : KeyEncapsulation<MLKEMPublicKeyParameters, MLKEMPrivateKeyParameters, ByteArray> {
    companion object {
        private val instances = ConcurrentHashMap<Boolean, MLKYBERKeyEncapsulation>()
        fun getInstance(strong: Boolean = false): MLKYBERKeyEncapsulation =
            instances.getOrPut(strong) { MLKYBERKeyEncapsulation(strong) }
    }

    private class Encapsulator(strong: Boolean) :
        KeyEncapsulation.KeyEncapsulator<MLKEMPublicKeyParameters, ByteArray> {
        private val random = getSecureRandom(strong)

        override fun encapsulate(publicKey: MLKEMPublicKeyParameters): Pair<ByteArray, ByteArray> {
            val generator = MLKEMGenerator(random)
            val encapsulated = generator.generateEncapsulated(publicKey)
            val secret = encapsulated.secret
            val ciphertext = encapsulated.encapsulation
            return Pair(ciphertext, secret)
        }
    }

    private class Decapsulator : KeyEncapsulation.KeyDecapsulator<MLKEMPrivateKeyParameters, ByteArray> {
        override fun decapsulate(
            ciphertext: ByteArray,
            privateKey: MLKEMPrivateKeyParameters
        ): ByteArray {
            val extractor = MLKEMExtractor(privateKey)
            return extractor.extractSecret(ciphertext)
        }
    }

    override val encapsulator: KeyEncapsulation.KeyEncapsulator<MLKEMPublicKeyParameters, ByteArray> =
        Encapsulator(strong)

    override val decapsulator: KeyEncapsulation.KeyDecapsulator<MLKEMPrivateKeyParameters, ByteArray> =
        Decapsulator()
}