package com.github.huymaster.server.api.security

interface KeyEncapsulationService {
    fun generateSecret(publicKey: ByteArray): Pair<ByteArray, ByteArray>
    fun decapsulate(ciphertext: ByteArray, privateKey: ByteArray): ByteArray
}