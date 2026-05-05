package com.github.huymaster.server.api.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KClass

interface EventPublisher {
    companion object {
        inline fun <reified E : Event> EventPublisher.subscribe(
            listener: EventListener<E>
        ): Subscription = subscribe(E::class, listener)

        inline fun <reified E : Event> EventPublisher.subscribeOn(
            job: Job,
            listener: EventListener<E>
        ): Subscription {
            val subscription = subscribe(E::class, listener)
            job.invokeOnCompletion { subscription.cancel() }
            return subscription
        }

        inline fun <reified E : Event> EventPublisher.subscribeOn(
            scope: CoroutineScope,
            listener: EventListener<E>
        ): Subscription {
            val job = scope.coroutineContext[Job]
            return if (job != null)
                subscribeOn(job, listener)
            else
                subscribe(E::class, listener)
        }
    }

    fun <E : Event> subscribe(eventType: KClass<E>, listener: EventListener<E>): Subscription

    suspend fun publish(event: Event)
}