package com.github.huymaster.server.core.database

object DatabaseConstants {
    const val USER_TABLE = "user"
    const val USER_ID_COLUMN = "user_id"
    const val USERNAME_COLUMN = "username"

    const val USER_CREDENTIAL_TABLE = "user_credential"
    const val PASSKEY_COLUMN = "passkey"

    const val USER_ROLE_TABLE = "user_role"
    const val ROLE_COLUMN = "role"

    const val USER_TOKEN_TABLE = "user_token"
    const val TOKEN_COLUMN = "token"
    const val IS_REVOKED_COLUMN = "is_revoked"
    const val EXPIRED_AT_COLUMN = "expired_at"
    const val USER_TOKEN_OWNER_COLUMN = "owner"

    const val USER_INFO_TABLE = "user_info"
    const val FIRST_NAME_COLUMN = "first_name"
    const val LAST_NAME_COLUMN = "last_name"
    const val DATE_OF_BIRTH_COLUMN = "date_of_birth"
    const val EMAIL_COLUMN = "email"
    const val PHONE_COLUMN = "phone"
    const val DISPLAY_NAME_COLUMN = "display_name"
    const val AVATAR_URL_COLUMN = "avatar_url"
    const val BIO_COLUMN = "bio"
    const val GENDER_COLUMN = "gender"

    const val FILE_METADATA_TABLE = "file_metadata"
    const val FILE_ID_COLUMN = "file_id"
    const val OBJECT_KEY_COLUMN = "object_key"
    const val FILE_NAME_COLUMN = "file_name"
    const val CONTENT_TYPE_COLUMN = "content_type"
    const val FILE_SIZE_COLUMN = "file_size"
    const val ETAG_COLUMN = "etag"
    const val FILE_OWNER_COLUMN = "owner"
    const val IS_PUBLIC_COLUMN = "is_public"
    const val FILE_CREATED_AT_COLUMN = "created_at"
    const val LAST_MODIFIED_COLUMN = "last_modified"

    const val USER_DEVICE_TABLE = "user_device"
    const val DEVICE_ID_COLUMN = "device_id"
    const val DEVICE_NAME_COLUMN = "device_name"
    const val DEVICE_CREATED_AT_COLUMN = "created_at"
    const val LAST_SEEN_COLUMN = "last_seen"
    const val REGISTRATION_ID_COLUMN = "registration_id"
    const val MLKEM_PUBLIC_KEY_COLUMN = "mlkem_public_key"
    const val ED_PUBLIC_KEY_COLUMN = "ed_public_key"
}