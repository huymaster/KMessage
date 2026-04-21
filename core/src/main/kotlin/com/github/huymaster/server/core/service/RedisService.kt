@file:Suppress("OPT_IN_USAGE")

package com.github.huymaster.server.core.service

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

class RedisService(
    connection: StatefulRedisConnection<String, String>
) : Service, RedisCoroutinesCommands<String, String> by connection.coroutines()