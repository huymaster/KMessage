package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface UserTokenEntity : Entity<UserTokenEntity> {
    companion object : Entity.Factory<UserTokenEntity>()

    var token: String
    var isRevoked: Boolean
    var expiresAt: Instant
    var owner: UUID
}