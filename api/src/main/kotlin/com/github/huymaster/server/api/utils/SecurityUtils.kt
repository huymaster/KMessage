package com.github.huymaster.server.api.utils

import java.nio.ByteBuffer
import java.security.SecureRandom

fun getSecureRandom(strong: Boolean): SecureRandom =
    if (strong) SecureRandom.getInstanceStrong() else SecureRandom()

fun getRandomBytes(sizeBytes: Int = 16, strong: Boolean = false): ByteArray {
    require(sizeBytes > 0) { "sizeBytes must be greater than 0" }
    val salt = ByteArray(sizeBytes)
    val random = getSecureRandom(strong)
    random.nextBytes(salt)
    return salt
}

fun mergeSalt(salt: ByteArray, key: ByteArray): ByteArray {
    require(salt.size + key.size <= Int.MAX_VALUE)
    val bufferSize = salt.size + key.size + (Int.SIZE_BYTES * 2)
    val buffer = ByteBuffer.allocate(bufferSize)
    buffer.putInt(salt.size)
    buffer.put(salt)
    buffer.putInt(key.size)
    buffer.put(key)
    return buffer.array()
}

fun splitSalt(input: ByteArray): Pair<ByteArray, ByteArray> {
    val buffer = ByteBuffer.wrap(input)
    val saltSize = buffer.int
    val salt = ByteArray(saltSize)
    buffer.get(salt)
    val keySize = buffer.int
    val key = ByteArray(keySize)
    buffer.get(key)
    return Pair(salt, key)
}