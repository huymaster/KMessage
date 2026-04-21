package com.github.huymaster.server.core.database.entity

import com.github.huymaster.server.api.models.common.UserRole
import org.ktorm.entity.Entity
import java.util.*

interface UserRoleEntity : Entity<UserRoleEntity> {
    companion object : Entity.Factory<UserRoleEntity>()

    var userId: UUID
    var role: UserRole
}