package com.github.huymaster.server.api.security

/**
 * A generic interface for cryptographic Key Derivation Functions (KDF).
 * Implementations process a high-entropy master key or a password to derive
 * cryptographically strong secret key.
 *
 * @param P the type of the password.
 * @param S the type of the derived secret key.
 */
interface KeyDerivation<in P, out S> {
    /**
     * Derives a cryptographic key from the given password and salt.
     *
     * This method acts as a one-step or two-step key derivation function (Extract-and-Expand)
     * to transform input keying material into a cryptographically strong symmetric key.
     *
     * @param password The input keying material or password.
     * @param salt A distinct space of nonces or random bytes to ensure unique outputs and prevent rainbow table attacks.
     * @return The derived secret key of type [S].
     */
    fun deriveKey(password: P, salt: ByteArray): S
}