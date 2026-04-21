package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.OneTimePrekeysEntity
import org.ktorm.schema.boolean
import org.ktorm.schema.bytes
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid

object OneTimePrekeysTable : BaseTable<OneTimePrekeysEntity>(DatabaseConstants.ONE_TIME_PREKEYS_TABLE) {
    val keyId = uuid(DatabaseConstants.ONE_TIME_PREKEYS_ID_COLUMN).primaryKey()
        .bindTo { it.keyId }

    val deviceId = uuid(DatabaseConstants.DEVICE_ID_COLUMN)
        .bindTo { it.deviceId }

    val key = bytes(DatabaseConstants.ONE_TIME_PREKEYS_KEY_COLUMN)
        .bindTo { it.key }

    val isUsed = boolean(DatabaseConstants.ONE_TIME_PREKEYS_IS_USED_COLUMN)
        .bindTo { it.isUsed }

    val createdAt = timestamp(DatabaseConstants.ONE_TIME_PREKEYS_CREATED_AT_COLUMN)
        .bindTo { it.createdAt }
}