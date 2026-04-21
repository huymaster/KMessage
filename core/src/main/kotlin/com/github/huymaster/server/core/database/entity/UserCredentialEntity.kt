package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.util.*

interface UserCredentialEntity : Entity<UserCredentialEntity> {
    companion object : Entity.Factory<UserCredentialEntity>()

    var userId: UUID
    var passkey: ByteArray
}