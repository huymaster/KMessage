package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserTokenEntity
import org.ktorm.schema.boolean
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object UserTokenTable : BaseTable<UserTokenEntity>(DatabaseConstants.USER_TOKEN_TABLE) {
    val token = varchar(DatabaseConstants.TOKEN_COLUMN)
        .bindTo { it.token }
        .primaryKey()

    val isRevoked = boolean(DatabaseConstants.IS_REVOKED_COLUMN)
        .bindTo { it.isRevoked }

    val expiredAt = timestamp(DatabaseConstants.EXPIRED_AT_COLUMN)
        .bindTo { it.expiresAt }

    val owner = uuid(DatabaseConstants.USER_TOKEN_OWNER_COLUMN)
        .bindTo { it.owner }
}