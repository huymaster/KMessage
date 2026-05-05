package com.github.huymaster.server.api.event

interface Subscription {
    companion object {
        fun <T : EventListener<*>> default(listener: T, onCancel: (T) -> Unit) = object : Subscription {
            override fun cancel() {
                runCatching { onCancel(listener) }
            }
        }
    }

    fun cancel()
}