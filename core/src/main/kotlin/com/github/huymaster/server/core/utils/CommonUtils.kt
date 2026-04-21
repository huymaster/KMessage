package com.github.huymaster.server.core.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class CircuitBreaker private constructor(config: Config) {
    data class Config(
        var minimumNumberOfCalls: Int,
        var slidingWindowSize: Int,
        var waitDurationInOpenState: Duration,
        var failureRateThreshold: Double,
        var permittedCallsInHalfOpenState: Int,
    )

    class CircuitBreakerException(message: String, override val cause: Throwable? = null) : RuntimeException(message)

    enum class State { CLOSED, OPEN, HALF_OPEN }
    private enum class ExecutionResult { SUCCESS, FAILURE }

    companion object {
        private val DEFAULT_CONFIG = Config(
            minimumNumberOfCalls = 10,
            slidingWindowSize = 50,
            waitDurationInOpenState = Duration.ofSeconds(10),
            failureRateThreshold = 0.5,
            permittedCallsInHalfOpenState = 5,
        )

        operator fun invoke(configuration: Config.() -> Unit = {}): CircuitBreaker {
            val config = DEFAULT_CONFIG.copy().apply(configuration)
            return CircuitBreaker(config)
        }
    }

    private val lock = Mutex()
    private val config: Config = config.copy()
    private val _state = AtomicReference(State.CLOSED)
    private val openTimestamp = AtomicReference(Instant.EPOCH)

    private val history = ArrayDeque<ExecutionResult>()
    private var failureCount = 0

    val state: State get() = _state.get()

    init {
        verifyConfiguration()
    }

    suspend fun <T> execute(block: suspend () -> T): T = try {
        evaluateStateTransition()
        when (_state.get()) {
            State.OPEN -> throw CircuitBreakerException("Circuit is OPEN")
            State.HALF_OPEN, State.CLOSED -> handler(block)
        }
    } catch (e: Exception) {
        throw e as? CircuitBreakerException ?: CircuitBreakerException("Error executing", e)
    }

    private suspend fun <T> handler(block: suspend () -> T): T = try {
        val result = block()
        recordResult(ExecutionResult.SUCCESS)
        result
    } catch (e: Exception) {
        recordResult(ExecutionResult.FAILURE)
        throw e
    }

    private suspend fun recordResult(result: ExecutionResult) = lock.withLock {
        when (_state.get()) {
            State.CLOSED -> {
                if (history.size >= config.slidingWindowSize) {
                    val oldest = history.removeFirst()
                    if (oldest == ExecutionResult.FAILURE) failureCount--
                }
                history.addLast(result)
                if (result == ExecutionResult.FAILURE) failureCount++
                if (history.size >= config.minimumNumberOfCalls) {
                    val failureRate = failureCount.toDouble() / history.size
                    if (failureRate >= config.failureRateThreshold) transitionToOpenState()
                }
            }

            State.HALF_OPEN -> {
                if (result == ExecutionResult.FAILURE)
                    transitionToOpenState()
                else {
                    history.addLast(result)
                    if (history.size >= config.permittedCallsInHalfOpenState) transitionToClosedState()
                }
            }

            State.OPEN -> {
                // Request not supposed to be allowed
            }
        }
    }

    private suspend fun evaluateStateTransition() = lock.withLock {
        if (_state.get() == State.OPEN) {
            val elapsed = Duration.between(openTimestamp.get(), Instant.now())
            if (elapsed > config.waitDurationInOpenState && _state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                history.clear()
                failureCount = 0
            }
        }
    }

    private fun transitionToOpenState() {
        _state.set(State.OPEN)
        openTimestamp.set(Instant.now())
        history.clear()
        failureCount = 0
    }

    private fun transitionToClosedState() {
        _state.set(State.CLOSED)
        openTimestamp.set(Instant.EPOCH)
        history.clear()
        failureCount = 0
    }

    private fun verifyConfiguration() {
        require(config.slidingWindowSize > 0) { "slidingWindowSize must be greater than 0" }
        require(config.failureRateThreshold in 0.0..1.0) { "failureRateThreshold must be between 0.0 and 1.0" }
        require(config.waitDurationInOpenState.isPositive) { "waitDurationInOpenState must be positive" }
        require(config.permittedCallsInHalfOpenState in 1..config.slidingWindowSize) { "permittedCallsInHalfOpenState must be between 1 and slidingWindowSize" }
        require(config.minimumNumberOfCalls in 1..config.slidingWindowSize) { "minimumNumberOfCalls must be between 1 and slidingWindowSize" }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("CircuitBreaker[")
        builder.append("state=").append(_state.get())
        if (_state.get() == State.OPEN) {
            val remaining = Duration.between(Instant.now(), openTimestamp.get().plus(config.waitDurationInOpenState))
            if (remaining.isNegative)
                builder.append(", close in 0s")
            else
                builder.append(", close in ").append(remaining)
        }
        builder.append("]")
        return builder.toString()
    }
}

@Serializable
class UUIDv7(
    private val mostSigBits: Long,
    private val leastSigBits: Long
) : Comparable<UUIDv7> {
    companion object {
        private val secure = SecureRandom()

        fun fromString(uuid: String): UUIDv7 {
            val uuid: UUID = UUID.fromString(uuid)
            require((uuid.mostSignificantBits shr 12) and 0xF == 0x7L) { "Not a UUIDv7" }
            return UUIDv7(uuid.mostSignificantBits, uuid.leastSignificantBits)
        }

        fun randomUUID(): UUIDv7 {
            val timestamp = System.currentTimeMillis() and 0xFFFFFFFFFFFFL
            val randomBytes = ByteArray(10)
            secure.nextBytes(randomBytes)

            var msb = (timestamp shl 16)
            msb = msb or (0x7000L)
            msb = msb or ((randomBytes[0].toLong() and 0x0F) shl 8)
            msb = msb or (randomBytes[1].toLong() and 0xFF)

            var lsb = (randomBytes[2].toLong() and 0x3F) shl 56
            lsb = lsb or (Long.MIN_VALUE)

            for (i in 3..9) {
                val shift = (9 - i) * 8
                lsb = lsb or ((randomBytes[i].toLong() and 0xFF) shl shift)
            }

            return UUIDv7(msb, lsb)
        }

        fun UUID.toUUIDv7(): UUIDv7 = UUIDv7(mostSignificantBits, leastSignificantBits)
    }

    val mostSignificantBits: Long = mostSigBits
    val leastSignificantBits: Long = leastSigBits

    init {
        require((mostSignificantBits shr 12) and 0xF == 0x7L) { "Not a UUIDv7" }
    }

    override fun compareTo(other: UUIDv7): Int {
        val msbCompare = java.lang.Long.compareUnsigned(this.mostSigBits, other.mostSigBits)
        if (msbCompare != 0) return msbCompare

        return java.lang.Long.compareUnsigned(this.leastSigBits, other.leastSigBits)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UUIDv7) return false
        return mostSigBits == other.mostSigBits && leastSigBits == other.leastSigBits
    }

    override fun hashCode(): Int {
        val msbHash = (mostSigBits xor (mostSigBits ushr 32)).toInt()
        val lsbHash = (leastSigBits xor (leastSigBits ushr 32)).toInt()
        return 31 * msbHash + lsbHash
    }

    override fun toString(): String {
        return UUID(mostSigBits, leastSigBits).toString()
    }

    fun toUUID(): UUID = UUID(mostSigBits, leastSigBits)
}

suspend inline fun <T> runRetry(
    attempts: Int = 3,
    initialDelayMs: Long = 100L,
    maxDelayMs: Long = 1_000L,
    factor: Double = 2.0,
    crossinline shouldRetry: (Throwable) -> Boolean = { it !is Error },
    crossinline onRetry: (Throwable, Int) -> Unit = { _, _ -> },
    block: suspend () -> T
): T {
    require(attempts > 0) { "attempts must be at least 1" }
    require(initialDelayMs >= 0) { "initialDelayMs must be non-negative" }
    require(maxDelayMs >= initialDelayMs) { "maxDelayMs must be >= initialDelayMs" }
    require(factor > 1.0) { "factor must be greater than 1.0" }

    var currentDelay = initialDelayMs
    repeat(attempts - 1) { attemptIndex ->
        try {
            return block()
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            if (!shouldRetry(e)) throw e

            onRetry(e, attemptIndex + 1)

            if (currentDelay > 0) {
                val capped = currentDelay.coerceAtMost(maxDelayMs)
                val jitterDelay = Random.nextLong(0, capped)
                delay(jitterDelay.milliseconds)
            }

            val nextDelay =
                if (currentDelay == 0L) 1.0
                else currentDelay.toDouble() * factor

            currentDelay = nextDelay
                .coerceAtMost(maxDelayMs.toDouble())
                .toLong()
        }
    }

    return block()
}

suspend inline fun <T> runRetryWithContext(
    context: CoroutineContext,
    attempts: Int = 3,
    initialDelayMs: Long = 100L,
    maxDelayMs: Long = 1_000L,
    factor: Double = 2.0,
    crossinline shouldRetry: (Throwable) -> Boolean = { it !is Error },
    crossinline onRetry: (Throwable, Int) -> Unit = { _, _ -> },
    crossinline block: suspend () -> T
): T = withContext(context) {
    runRetry(
        attempts = attempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        factor = factor,
        shouldRetry = shouldRetry,
        onRetry = onRetry,
        block = block
    )
}

suspend inline fun <T> runRetryWithBreaker(
    breaker: CircuitBreaker,
    attempts: Int = 3,
    initialDelayMs: Long = 100L,
    maxDelayMs: Long = 1_000L,
    factor: Double = 2.0,
    crossinline shouldRetry: (Throwable) -> Boolean = { it !is Error },
    crossinline onRetry: (Throwable, Int) -> Unit = { _, _ -> },
    crossinline block: suspend () -> T
): T = breaker.execute {
    runRetry(
        attempts = attempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        factor = factor,
        shouldRetry = shouldRetry,
        onRetry = onRetry,
        block = block
    )
}