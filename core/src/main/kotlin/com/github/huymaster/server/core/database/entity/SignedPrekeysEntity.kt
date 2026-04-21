package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface SignedPrekeysEntity : Entity<SignedPrekeysEntity> {
    companion object : Entity.Factory<SignedPrekeysEntity>()

    var keyId: UUID
    var deviceId: UUID
    var key: ByteArray
    var signature: ByteArray
    var isActive: Boolean
    var createdAt: Instant
    var expiresAt: Instant
}