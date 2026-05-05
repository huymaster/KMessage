@file:Suppress("OPT_IN_USAGE")

package com.github.huymaster.server.core.service

import io.lettuce.core.RedisConnectionStateListener
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.api.push.PushListener
import io.lettuce.core.api.push.PushMessage
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands

class RedisService(
    private val connection: StatefulRedisPubSubConnection<String, String>
) : Service, RedisCoroutinesCommands<String, String> by connection.coroutines() {
    val async: RedisPubSubAsyncCommands<String, String> get() = connection.async()
    val reactive: RedisPubSubReactiveCommands<String, String> get() = connection.reactive()
    val sync: RedisPubSubCommands<String, String> get() = connection.sync()

    fun addListener(p0: PushListener) = connection.addListener(p0)
    fun addListener(p0: (PushMessage) -> Unit) = connection.addListener(p0)
    fun addListener(p0: RedisConnectionStateListener) = connection.addListener(p0)
    fun addListener(p0: RedisPubSubListener<String, String>) = connection.addListener(p0)

    fun removeListener(p0: PushListener) = connection.removeListener(p0)
    fun removeListener(p0: (PushMessage) -> Unit) = connection.removeListener(p0)
    fun removeListener(p0: RedisConnectionStateListener) = connection.removeListener(p0)
    fun removeListener(p0: RedisPubSubListener<String, String>) = connection.removeListener(p0)
}