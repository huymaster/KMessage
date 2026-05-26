package com.github.huymaster.server.api.security

import org.bouncycastle.crypto.params.*
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.crypto.signers.HashMLDSASigner
import org.bouncycastle.crypto.signers.MLDSASigner

object Ed25519DigitalSignature
    : DigitalSignature<Ed25519PublicKeyParameters, Ed25519PrivateKeyParameters, ByteArray, ByteArray> {
    override fun sign(
        privateKey: Ed25519PrivateKeyParameters,
        message: ByteArray
    ): ByteArray {
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    override fun verify(
        publicKey: Ed25519PublicKeyParameters,
        message: ByteArray,
        signature: ByteArray
    ): Boolean {
        val signer = Ed25519Signer()
        signer.init(false, publicKey)
        signer.update(message, 0, message.size)
        return signer.verifySignature(signature)
    }
}

object MLDSADigitalSignature
    : DigitalSignature<MLDSAPublicKeyParameters, MLDSAPrivateKeyParameters, ByteArray, ByteArray> {
    private fun selectSigner(parameters: MLDSAParameters) = if (parameters.isPreHash)
        HashMLDSASigner()
    else
        MLDSASigner()

    override fun sign(
        privateKey: MLDSAPrivateKeyParameters,
        message: ByteArray
    ): ByteArray {
        val signer = selectSigner(privateKey.parameters)
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    override fun verify(
        publicKey: MLDSAPublicKeyParameters,
        message: ByteArray,
        signature: ByteArray
    ): Boolean {
        val signer = selectSigner(publicKey.parameters)
        signer.init(false, publicKey)
        signer.update(message, 0, message.size)
        return signer.verifySignature(signature)
    }
}