package com.github.huymaster.server.api.security

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.security.Security

/**
 * A generic interface for generating cryptographic key pairs.
 * Implementations of this interface are responsible for creating a pair of
 * keys (typically a public and private key) according to specific cryptographic
 * algorithms.
 *
 * @param P the type of the public key.
 * @param S the type of the private (secret) key.
 */
interface KeyPairGenerator<out P, out S> {
    companion object {
        init {
            if (Security.getProviders(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
                Security.addProvider(BouncyCastlePQCProvider())
        }
    }

    fun generate(): Pair<P, S>
}