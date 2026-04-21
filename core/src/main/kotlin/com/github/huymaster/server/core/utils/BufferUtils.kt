package com.github.huymaster.server.core.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.math.min

class ByteBufferInputStream(private val buffer: ByteBuffer) : InputStream(), AutoCloseable {
    override fun read(): Int {
        if (!buffer.hasRemaining()) return -1
        return buffer.get().toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0

        val remaining = buffer.remaining()
        if (remaining == 0) return -1

        val bytesToRead = min(len, remaining)
        buffer.get(b, off, bytesToRead)
        return bytesToRead
    }

    override fun available(): Int = buffer.remaining()

    override fun markSupported(): Boolean = true

    override fun mark(readlimit: Int) {
        buffer.mark()
    }

    override fun reset() {
        buffer.reset()
    }

    override fun close() {
        super.close()
    }
}

class ByteBufferOutputStream(private val buffer: ByteBuffer) : OutputStream() {
    @Throws(IOException::class)
    override fun write(b: Int) {
        if (!buffer.hasRemaining()) throw IOException("Buffer overflow: No remaining space to write.")
        buffer.put(b.toByte())
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || off > b.size || len < 0 || (off + len) > b.size || (off + len) < 0) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return
        }

        if (len > buffer.remaining()) {
            throw IOException("Buffer overflow: Requested write length $len exceeds remaining capacity ${buffer.remaining()}.")
        }

        buffer.put(b, off, len)
    }

    override fun close() {
        super.close()
    }
}