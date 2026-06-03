package com.github.huymaster.server.api.security

/**
 * A generic interface for generating cryptographic key.
 *
 * @param [S] the type of secret key.
 */
interface KeyGenerator<S> : Security {
    /**
     * Generates a new cryptographic key (symmetric key).
     *
     * @return [S] type of secret key.
     */
    fun generate(): S
}