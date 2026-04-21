package com.github.huymaster.server.core.database.table

import com.github.huymaster.server.core.database.DatabaseConstants
import com.github.huymaster.server.core.database.entity.FileMetadataEntity
import org.ktorm.schema.*

object FileMetadataTable : BaseTable<FileMetadataEntity>(DatabaseConstants.FILE_METADATA_TABLE) {
    val fileId = uuid(DatabaseConstants.FILE_ID_COLUMN).primaryKey()
        .bindTo { it.fileId }

    val objectKey = varchar(DatabaseConstants.OBJECT_KEY_COLUMN)
        .bindTo { it.objectKey }

    val filename = varchar(DatabaseConstants.FILE_NAME_COLUMN)
        .bindTo { it.filename }

    val contentType = varchar(DatabaseConstants.CONTENT_TYPE_COLUMN)
        .bindTo { it.contentType }

    val fileSize = long(DatabaseConstants.FILE_SIZE_COLUMN)
        .bindTo { it.fileSize }

    val etag = varchar(DatabaseConstants.ETAG_COLUMN)
        .bindTo { it.etag }

    val owner = uuid(DatabaseConstants.FILE_OWNER_COLUMN)
        .bindTo { it.owner }

    val isPublic = boolean(DatabaseConstants.IS_PUBLIC_COLUMN)
        .bindTo { it.isPublic }

    val createdAt = timestamp(DatabaseConstants.FILE_CREATED_AT_COLUMN)
        .bindTo { it.createdAt }

    val lastModified = timestamp(DatabaseConstants.LAST_MODIFIED_COLUMN)
        .bindTo { it.lastModified }
}