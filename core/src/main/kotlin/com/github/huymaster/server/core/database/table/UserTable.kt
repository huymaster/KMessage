package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserEntity
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object UserTable : BaseTable<UserEntity>(DatabaseConstants.USER_TABLE) {
    val userId = uuid(DatabaseConstants.USER_ID_COLUMN)
        .bindTo { it.userId }
        .primaryKey()

    val username = varchar(DatabaseConstants.USERNAME_COLUMN)
        .bindTo { it.username }
}