package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface OneTimePrekeysEntity : Entity<OneTimePrekeysEntity> {
    companion object : Entity.Factory<OneTimePrekeysEntity>()

    var keyId: UUID
    var deviceId: UUID
    var key: ByteArray
    var isUsed: Boolean
    var createdAt: Instant
}