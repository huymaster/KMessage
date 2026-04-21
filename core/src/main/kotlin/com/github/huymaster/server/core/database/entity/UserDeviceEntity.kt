package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface UserDeviceEntity : Entity<UserDeviceEntity> {
    companion object : Entity.Factory<UserDeviceEntity>()

    var deviceId: UUID
    var userId: UUID
    var deviceName: String?
    var identityKey: ByteArray
    var createdAt: Instant
    var lastSeen: Instant
    var registrationId: Int
}