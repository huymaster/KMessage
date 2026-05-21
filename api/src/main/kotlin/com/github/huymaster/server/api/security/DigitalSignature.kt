package com.github.huymaster.server.api.security

/**
 * A generic interface for core digital signature operations.
 * Implementations provide data integrity, authentication, and non-repudiation
 * by signing messages and verifying the validity of generated signatures.
 *
 * @param P the type of the public key used for verification.
 * @param S the type of the private (secret) key used for signing.
 * @param M the type of the message to be signed or verified.
 * @param R the type of the generated signature output.
 */
interface DigitalSignature<in P, in S, in M, R> {
    /**
     * Signs the given message using the private key.
     *
     * @param privateKey The signer's private key of type [S].
     * @param message The input payload of type [M] to be signed.
     * @return The cryptographic signature of type [R].
     */
    fun sign(privateKey: S, message: M): R

    /**
     * Verifies the cryptographic signature of the given message using the public key.
     *
     * @param publicKey The signer's public key of type [P].
     * @param message The original payload of type [M] that was signed.
     * @param signature The signature of type [R] to be validated.
     * @return `true` if the signature is valid, `false` otherwise.
     */
    fun verify(publicKey: P, message: M, signature: R): Boolean
}