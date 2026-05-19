package com.github.huymaster.server.api.security

import com.github.huymaster.server.api.utils.getSecureRandom
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.*
import java.util.concurrent.ConcurrentHashMap

class MLKYBERKeyPairGenerator private constructor(
    private val params: Parameters
) : KeyPairGenerator<MLKEMPublicKeyParameters, MLKEMPrivateKeyParameters> {
    private data class Parameters(val params: MLKEMParameters, val strong: Boolean)

    companion object {
        private val instances = ConcurrentHashMap<Parameters, MLKYBERKeyPairGenerator>()

        fun getInstance(
            params: MLKEMParameters = MLKEMParameters.ml_kem_768,
            strong: Boolean = false
        ): MLKYBERKeyPairGenerator {
            val params = Parameters(params, strong)
            return instances.getOrPut(params) { MLKYBERKeyPairGenerator(params) }
        }
    }

    override fun generate(): Pair<MLKEMPublicKeyParameters, MLKEMPrivateKeyParameters> {
        val gen = org.bouncycastle.crypto.generators.MLKEMKeyPairGenerator()
        gen.init(MLKEMKeyGenerationParameters(getSecureRandom(params.strong), params.params))
        val pair = gen.generateKeyPair()

        val public = pair.public as? MLKEMPublicKeyParameters
            ?: throw IllegalStateException("Failed to generate ML-KEM public key")

        val private = pair.private as? MLKEMPrivateKeyParameters
            ?: throw IllegalStateException("Failed to generate ML-KEM private key")

        return Pair(public, private)
    }
}

class ED25519KeyPairGenerator private constructor(
    private val strong: Boolean
) : KeyPairGenerator<Ed25519PublicKeyParameters, Ed25519PrivateKeyParameters> {
    companion object {
        private val instances = ConcurrentHashMap<Boolean, ED25519KeyPairGenerator>()

        fun getInstance(strong: Boolean = false): ED25519KeyPairGenerator =
            instances.getOrPut(strong) { ED25519KeyPairGenerator(strong) }
    }

    override fun generate(): Pair<Ed25519PublicKeyParameters, Ed25519PrivateKeyParameters> {
        val gen = Ed25519KeyPairGenerator()
        gen.init(Ed25519KeyGenerationParameters(getSecureRandom(strong)))
        val pair = gen.generateKeyPair()

        val public = pair.public as? Ed25519PublicKeyParameters
            ?: throw IllegalStateException("Failed to generate ED25519 public key")

        val private = pair.private as? Ed25519PrivateKeyParameters
            ?: throw IllegalStateException("Failed to generate ED25519 private key")

        return Pair(public, private)
    }
}