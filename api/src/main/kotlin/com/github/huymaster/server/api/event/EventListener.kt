package com.github.huymaster.server.api.event

interface EventListener<E : Event> {
    companion object {
        fun <E : Event> default(
            onEvent: suspend (E) -> Unit,
            onError: suspend (Exception) -> Unit = { it.printStackTrace() },
            onCancel: () -> Unit = {}
        ) = object : EventListener<E> {
            override suspend fun onEvent(event: E) = onEvent(event)

            override suspend fun onError(exception: Exception) = onError(exception)

            override fun onCancel() = onCancel()
        }
    }

    suspend fun onEvent(event: E)
    suspend fun onError(exception: Exception)
    fun onCancel()
}