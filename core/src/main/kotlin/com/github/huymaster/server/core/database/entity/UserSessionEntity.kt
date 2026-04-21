package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface UserSessionEntity : Entity<UserSessionEntity> {
    companion object : Entity.Factory<UserSessionEntity>()

    var sessionId: UUID
    var initiatorDeviceId: UUID
    var receiverDeviceId: UUID
    var signedPrekeysId: UUID
    var oneTimePrekeysId: UUID?
    var isActive: Boolean
    var messageSequence: Long
    var createdAt: Instant
    var lastMessageAt: Instant
}