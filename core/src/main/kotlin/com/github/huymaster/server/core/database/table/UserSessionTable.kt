package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserSessionEntity
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid

object UserSessionTable : BaseTable<UserSessionEntity>(DatabaseConstants.USER_SESSION_TABLE) {
    val sessionId = uuid(DatabaseConstants.SESSION_ID_COLUMN).primaryKey()
        .bindTo { it.sessionId }

    val initiatorDeviceId = uuid(DatabaseConstants.INITIATOR_DEVICE_ID_COLUMN)
        .bindTo { it.initiatorDeviceId }

    val receiverDeviceId = uuid(DatabaseConstants.RECEIVER_DEVICE_ID_COLUMN)
        .bindTo { it.receiverDeviceId }

    val signedPrekeysId = uuid(DatabaseConstants.SESSION_SIGNED_PREKEYS_ID_COLUMN)
        .bindTo { it.signedPrekeysId }

    val oneTimePrekeysId = uuid(DatabaseConstants.SESSION_ONE_TIME_PREKEYS_ID_COLUMN)
        .bindTo { it.oneTimePrekeysId }

    val isActive = boolean(DatabaseConstants.SIGNED_PREKEYS_IS_ACTIVE_COLUMN)
        .bindTo { it.isActive }

    val messageSequence = long(DatabaseConstants.MESSAGE_SEQUENCE_COLUMN)
        .bindTo { it.messageSequence }

    val createdAt = timestamp(DatabaseConstants.SESSION_CREATED_AT_COLUMN)
        .bindTo { it.createdAt }

    val lastMessageAt = timestamp(DatabaseConstants.LAST_MESSAGE_AT_COLUMN)
        .bindTo { it.lastMessageAt }
}