package com.github.huymaster.server.api.security

/**
 * A generic interface for generating cryptographic key pairs.
 * Implementations of this interface are responsible for creating a pair of
 * keys (typically a public and private key) according to specific cryptographic
 * algorithms, including Post-Quantum Cryptography (PQC) standards.
 *
 * @param P the type of the public key.
 * @param S the type of the private (secret) key.
 */
interface KeyPairGenerator<out P, out S> : Security {
    /**
     * Generates a new cryptographic key pair containing a public key and a private key.
     *
     *  @return A [Pair] containing:
     *  - Public key [P]
     *  - Private key [S].
     */
    fun generate(): Pair<P, S>
}