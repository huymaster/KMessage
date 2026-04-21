package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.api.models.common.Gender
import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.UserInfoEntity
import org.ktorm.schema.date
import org.ktorm.schema.enum
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object UserInfoTable : BaseTable<UserInfoEntity>(DatabaseConstants.USER_INFO_TABLE) {
    val userId = uuid(DatabaseConstants.USER_ID_COLUMN).primaryKey()
        .bindTo { it.userId }

    val firstName = varchar(DatabaseConstants.FIRST_NAME_COLUMN)
        .bindTo { it.firstName }

    val lastName = varchar(DatabaseConstants.LAST_NAME_COLUMN)
        .bindTo { it.lastName }

    val dateOfBirth = date(DatabaseConstants.DATE_OF_BIRTH_COLUMN)
        .bindTo { it.dateOfBirth }

    val email = varchar(DatabaseConstants.EMAIL_COLUMN)
        .bindTo { it.email }

    val phone = varchar(DatabaseConstants.PHONE_COLUMN)
        .bindTo { it.phone }

    val displayName = varchar(DatabaseConstants.DISPLAY_NAME_COLUMN)
        .bindTo { it.displayName }

    val avatarUrl = varchar(DatabaseConstants.AVATAR_URL_COLUMN)
        .bindTo { it.avatarUrl }

    val bio = varchar(DatabaseConstants.BIO_COLUMN)
        .bindTo { it.bio }

    val gender = enum<Gender>(DatabaseConstants.GENDER_COLUMN)
        .bindTo { it.gender }
}