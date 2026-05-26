package com.github.huymaster.server.api.utils

import java.nio.ByteBuffer

fun mergeByteArrays(vararg arrays: ByteArray): ByteArray {
    if (arrays.isEmpty()) return ByteArray(0)
    if (arrays.size == 1) return arrays[0]
    val buffer = ByteBuffer.allocate(arrays.sumOf { it.size })
    arrays.forEach { buffer.put(it) }
    return buffer.array()
}