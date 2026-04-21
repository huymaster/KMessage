package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.api.models.common.UserRole
import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserRoleEntity
import org.ktorm.schema.enum
import org.ktorm.schema.uuid

object UserRoleTable : BaseTable<UserRoleEntity>(DatabaseConstants.USER_ROLE_TABLE) {
    val userId = uuid(DatabaseConstants.USER_ID_COLUMN)
        .bindTo { it.userId }
        .primaryKey()

    val role = enum<UserRole>(DatabaseConstants.ROLE_COLUMN)
        .bindTo { it.role }
}