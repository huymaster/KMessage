package com.github.huymaster.server.api.security

/**
 * A generic interface for symmetric-key cryptographic ciphers.
 * Implementations of this interface provide data confidentiality by encrypting
 * plaintext into ciphertext and decrypting ciphertext back to its original form
 * using a shared symmetric key or cryptographic keying materia
 *
 * @param I the type of the plaintext input and decrypted output.
 * @param O the type of the encrypted ciphertext output and input.
 * @param K the type of the symmetric cryptographic key or parameter material required.
 */
interface SymmetricCipher<I, O, in K> {
    /**
     * Encrypts the provided plaintext into a secure ciphertext format.
     *
     * @param plaintext The unencrypted data of type [I] to be protected.
     * @param key The symmetric key of type [K] used to encrypt the payload.
     * @param iv The Initialization Vector (IV) or nonce required to ensure cryptographic randomness and security.
     * @return The cryptographically secured ciphertext of type [O].
     */
    fun encrypt(plaintext: I, key: K, iv: ByteArray): O

    /**
     * Decrypts the provided ciphertext back into its original plaintext format.
     *
     * @param ciphertext The encrypted data of type [O] to be deciphered.
     * @param key The symmetric key of type [K] used to decrypt the payload.
     * @param iv The Initialization Vector (IV) or nonce originally used during the encryption process.
     * @return The recovered, original plaintext of type [I].
     */
    fun decrypt(ciphertext: O, key: K, iv: ByteArray): I
}