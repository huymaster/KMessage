package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserDeviceEntity
import org.ktorm.schema.*

object UserDeviceTable : BaseTable<UserDeviceEntity>(DatabaseConstants.USER_DEVICE_TABLE) {
    val deviceId = uuid(DatabaseConstants.DEVICE_ID_COLUMN).primaryKey()
        .bindTo { it.deviceId }

    val userId = uuid(DatabaseConstants.USER_ID_COLUMN)
        .bindTo { it.userId }

    val deviceName = varchar(DatabaseConstants.DEVICE_NAME_COLUMN)
        .bindTo { it.deviceName }

    val createdAt = timestamp(DatabaseConstants.DEVICE_CREATED_AT_COLUMN)
        .bindTo { it.createdAt }

    val lastSeen = timestamp(DatabaseConstants.LAST_SEEN_COLUMN)
        .bindTo { it.lastSeen }

    val registrationId = int(DatabaseConstants.REGISTRATION_ID_COLUMN)
        .bindTo { it.registrationId }

    val mlkemPublicKey = bytes(DatabaseConstants.MLKEM_PUBLIC_KEY_COLUMN)
        .bindTo { it.mlkemPublicKey }

    val mldsaPublicKey = bytes(DatabaseConstants.MLDSA_PUBLIC_KEY_COLUMN)
        .bindTo { it.mldsaPublicKey }

    val edPublicKey = bytes(DatabaseConstants.ED_PUBLIC_KEY_COLUMN)
        .bindTo { it.edPublicKey }
}