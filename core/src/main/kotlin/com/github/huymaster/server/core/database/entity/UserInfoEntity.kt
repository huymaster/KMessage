package com.github.huymaster.server.core.database.entity

import com.github.huymaster.server.api.models.common.Gender
import org.ktorm.entity.Entity
import java.time.LocalDate
import java.util.*

interface UserInfoEntity : Entity<UserInfoEntity> {
    companion object : Entity.Factory<UserInfoEntity>()

    var userId: UUID
    var firstName: String?
    var lastName: String?
    var dateOfBirth: LocalDate?
    var email: String?
    var phone: String?
    var displayName: String?
    var avatarUrl: String?
    var bio: String?
    var gender: Gender
}