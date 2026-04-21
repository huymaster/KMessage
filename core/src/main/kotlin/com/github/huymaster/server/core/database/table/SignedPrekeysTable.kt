package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.SignedPrekeysEntity
import org.ktorm.schema.boolean
import org.ktorm.schema.bytes
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid

object SignedPrekeysTable : BaseTable<SignedPrekeysEntity>(DatabaseConstants.SIGNED_PREKEYS_TABLE) {
    val keyId = uuid(DatabaseConstants.SIGNED_PREKEYS_ID_COLUMN).primaryKey()
        .bindTo { it.keyId }

    val deviceId = uuid(DatabaseConstants.DEVICE_ID_COLUMN)
        .bindTo { it.deviceId }

    val key = bytes(DatabaseConstants.SIGNED_PREKEYS_KEY_COLUMN)
        .bindTo { it.key }

    val signature = bytes(DatabaseConstants.SIGNED_PREKEYS_SIGNATURE_COLUMN)
        .bindTo { it.signature }

    val isActive = boolean(DatabaseConstants.SIGNED_PREKEYS_IS_ACTIVE_COLUMN)
        .bindTo { it.isActive }

    val createdAt = timestamp(DatabaseConstants.SIGNED_PREKEYS_CREATED_AT_COLUMN)
        .bindTo { it.createdAt }

    val expiredAt = timestamp(DatabaseConstants.SIGNED_PREKEYS_EXPIRED_AT_COLUMN)
        .bindTo { it.expiresAt }
}