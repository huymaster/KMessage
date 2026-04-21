package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserCredentialEntity
import org.ktorm.schema.bytes
import org.ktorm.schema.uuid

object UserCredentialTable : BaseTable<UserCredentialEntity>(DatabaseConstants.USER_CREDENTIAL_TABLE) {
    val userId = uuid(DatabaseConstants.USER_ID_COLUMN)
        .bindTo { it.userId }
        .primaryKey()

    val passkey = bytes(DatabaseConstants.PASSKEY_COLUMN)
        .bindTo { it.passkey }
}