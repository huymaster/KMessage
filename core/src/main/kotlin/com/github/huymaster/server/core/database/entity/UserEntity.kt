package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.util.*

interface UserEntity : Entity<UserEntity> {
    companion object : Entity.Factory<UserEntity>()

    var userId: UUID
    var username: String
}