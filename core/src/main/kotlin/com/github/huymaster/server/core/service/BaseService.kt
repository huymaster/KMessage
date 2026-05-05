package com.github.huymaster.server.core.service

import com.github.huymaster.server.core.database.repository.Repository
import com.github.huymaster.server.core.database.table.BaseTable
import com.github.huymaster.server.core.utils.CircuitBreaker
import com.github.huymaster.server.core.utils.runRetry
import com.github.huymaster.server.core.utils.runRetryWithBreaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.ktorm.database.Database
import org.ktorm.database.Transaction
import org.ktorm.entity.Entity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

abstract class BaseService : Service {
    companion object {
        protected val breaker = CircuitBreaker()
    }

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)
    protected val database: Database by inject()
    protected val redis: RedisService by inject()
    protected val minio: MinioService by inject()

    protected fun <E : Entity<E>, T : BaseTable<E>> injectRepository(table: T): Lazy<Repository<E, T>> =
        inject { parametersOf(table) }

    protected fun <E : Entity<E>, T : BaseTable<E>> getRepository(table: T): Repository<E, T> =
        get { parametersOf(table) }

    protected suspend inline fun <R> transaction(
        context: CoroutineContext = Dispatchers.IO,
        crossinline block: suspend Transaction.() -> R
    ) = withContext(context) { runCatching { database.useTransaction { block(it) } } }

    protected suspend inline fun <R> transaction(
        mutex: Mutex,
        context: CoroutineContext = Dispatchers.IO,
        crossinline block: suspend Transaction.() -> R
    ) = mutex.withLock { transaction(context, block) }

    protected suspend inline fun <R> transactionWithRetry(
        attempts: Int = 3,
        initialDelayMs: Long = 100L,
        maxDelayMs: Long = 1_000L,
        factor: Double = 2.0,
        context: CoroutineContext = Dispatchers.IO,
        crossinline block: suspend Transaction.() -> R
    ) = runRetry(
        attempts = attempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        factor = factor
    ) {
        transaction(context, block)
    }

    protected suspend inline fun <R> transactionWithRetry(
        mutex: Mutex,
        attempts: Int = 3,
        initialDelayMs: Long = 100L,
        maxDelayMs: Long = 1_000L,
        factor: Double = 2.0,
        context: CoroutineContext = Dispatchers.IO,
        crossinline block: suspend Transaction.() -> R
    ) = runRetry(
        attempts = attempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        factor = factor
    ) {
        transaction(mutex, context, block)
    }

    protected suspend inline fun <R> transactionWithRetryBreaker(
        breaker: CircuitBreaker = Companion.breaker,
        attempts: Int = 3,
        initialDelayMs: Long = 100L,
        maxDelayMs: Long = 1_000L,
        factor: Double = 2.0,
        context: CoroutineContext = Dispatchers.IO,
        crossinline block: suspend Transaction.() -> R
    ) = runRetryWithBreaker(
        breaker = breaker,
        attempts = attempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        factor = factor
    ) {
        transaction(context, block)
    }

    protected suspend inline fun <R> transactionWithRetry(
        breaker: CircuitBreaker = Companion.breaker,
        mutex: Mutex,
        attempts: Int = 3,
        initialDelayMs: Long = 100L,
        maxDelayMs: Long = 1_000L,
        factor: Double = 2.0,
        context: CoroutineContext = Dispatchers.IO,
        crossinline block: suspend Transaction.() -> R
    ) = runRetryWithBreaker(
        breaker = breaker,
        attempts = attempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs,
        factor = factor
    ) {
        transaction(mutex, context, block)
    }
}