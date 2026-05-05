package com.github.huymaster.server.core.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sun.misc.Unsafe
import java.lang.ref.Cleaner
import java.lang.ref.Reference
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * A high-performance utility for managing off-heap [ByteBuffer] pools.
 * * This manager provides a pooling mechanism for direct buffers to reduce the overhead
 * of frequent native memory allocation and deallocation.
 * It leverages [Cleaner] for safe resource disposal and Coroutine [Channel] for
 * non-blocking buffer acquisition.
 *
 * Designed for systems requiring stable memory throughput, such as high-performance
 * networking or file I/O operations.
 *
 * @author HuyMaster
 */
object DirectMemoryPool {
    private val logger: Logger = LoggerFactory.getLogger(DirectMemoryPool::class.java)
    private val internalCleaner: Cleaner by lazy { Cleaner.create() }
    private val unsafe: Unsafe by lazy {
        val f = Unsafe::class.java.getDeclaredField("theUnsafe")
        f.isAccessible = true
        f.get(null) as Unsafe
    }

    @RequiresOptIn(
        level = RequiresOptIn.Level.ERROR,
        message = "Manual lifecycle management is dangerous. Use 'use' extension instead if possible."
    )
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    annotation class UnsafeDirectMemoryApi

    interface Provider : AutoCloseable {
        val name: String
        val bufferSize: Int

        /**
         * Attempts to acquire a buffer immediately without suspending.
         * @return A [ByteBuffer] if available, or `null` if the pool is exhausted.
         */
        @UnsafeDirectMemoryApi
        fun acquire(): ByteBuffer?

        /**
         * Blocks until a buffer is available.
         * @return A [ByteBuffer] from the pool.
         */
        @UnsafeDirectMemoryApi
        fun acquireBlocking(): ByteBuffer

        /**
         * Returns a buffer to the pool for reuse.
         * @param buffer The [ByteBuffer] to be recycled.
         */
        @UnsafeDirectMemoryApi
        fun release(buffer: ByteBuffer)

        /**
         * Executes the given [block] with a pooled buffer and ensures its return.
         * This is the recommended way to use the pool to prevent leaks.
         */
        suspend fun <R> use(block: suspend (ByteBuffer) -> R): R
    }

    private class ProviderImpl(
        pages: Int,
        poolSize: Int,
        override val name: String = "DirectPool-${poolIdGenerator.getAndIncrement()}"
    ) : Provider {

        companion object {
            private val poolIdGenerator = AtomicInteger(0)
            private const val OS_PAGE_SIZE = 4096
        }

        /**
         * Internal cleaning action to release native memory via Unsafe.
         * Static class to avoid capturing a strong reference to the Pool instance.
         */
        private class PoolDeallocator(
            private val unsafe: Unsafe,
            private val buffers: Array<ByteBuffer>
        ) : Runnable {
            override fun run() {
                logger.debug("Executing native memory deallocation for pool")
                buffers.forEach { buffer ->
                    runCatching {
                        unsafe.invokeCleaner(buffer)
                    }.onFailure {
                        logger.error("Failed to deallocate direct buffer", it)
                    }
                }
                logger.debug("Native memory deallocation completed for pool")
            }
        }

        override val bufferSize = OS_PAGE_SIZE * pages
        private val availableBuffers = Channel<ByteBuffer>(poolSize)
        private val isClosed = AtomicBoolean(false)
        private val allBuffers = Array(poolSize) { ByteBuffer.allocateDirect(bufferSize) }

        private val cleanable: Cleaner.Cleanable =
            internalCleaner.register(this, PoolDeallocator(unsafe, allBuffers))

        init {
            require(pages in 1..16) { "Pages must be between 1 and 16 (4KB to 64KB)" }
            require(poolSize in 1..64) { "Pool size must be between 1 and 64" }
            allBuffers.forEach {
                check(availableBuffers.trySend(it).isSuccess) { "Buffer channel initialization failed" }
            }
        }

        @UnsafeDirectMemoryApi
        override fun acquire(): ByteBuffer? {
            check(!isClosed.get()) { "Pool $name is already closed" }
            return availableBuffers.tryReceive().getOrNull()
        }

        @UnsafeDirectMemoryApi
        override fun acquireBlocking(): ByteBuffer {
            check(!isClosed.get()) { "Pool $name is already closed" }
            val buffer = runBlocking { availableBuffers.receive() }
            Reference.reachabilityFence(this)
            return buffer
        }

        @UnsafeDirectMemoryApi
        override fun release(buffer: ByteBuffer) {
            if (isClosed.get()) return
            buffer.clear()
            val result = availableBuffers.trySend(buffer)
            if (!result.isSuccess)
                logger.warn("Buffer returned to a full or closed channel in pool $name")
        }

        override suspend fun <R> use(block: suspend (ByteBuffer) -> R): R {
            val buffer = availableBuffers.receive()
            return try {
                buffer.clear()
                block(buffer)
            } finally {
                @OptIn(UnsafeDirectMemoryApi::class)
                release(buffer)
                Reference.reachabilityFence(buffer)
                Reference.reachabilityFence(this)
            }
        }

        override fun close() {
            if (isClosed.compareAndSet(false, true)) {
                availableBuffers.close()
                cleanable.clean()
                logger.info("Direct pool $name closed and resources deallocated")
            }
        }

        override fun toString(): String =
            "DirectPool(name='$name', pageSize=${bufferSize / 1024}KB, totalSize=${allBuffers.size})"
    }

    /**
     * Creates a new [Provider].
     *
     * @param pages Number of 4KB OS pages per buffer (e.g., 1 page = 4096 bytes).
     * @param poolSize Total number of buffers to keep in the pool.
     * @param name Optional custom name for monitoring and logging.
     * @return A managed buffer pool instance.
     */
    fun createProvider(pages: Int = 1, poolSize: Int = 8, name: String? = null): Provider {
        val pool = if (name != null)
            ProviderImpl(pages, poolSize, name)
        else
            ProviderImpl(pages, poolSize)
        logger.debug("Created direct pool: {}", pool)
        return pool
    }
}