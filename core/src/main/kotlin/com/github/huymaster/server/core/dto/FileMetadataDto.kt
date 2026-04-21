package com.github.huymaster.server.core.dto

import com.github.huymaster.server.core.database.entity.FileMetadataEntity
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

@Serializable
data class FileMetadataDto(
    val fileId: String,
    val objectKey: String,
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val etag: String,
    val owner: String,
    val isPublic: Boolean,
    val createdAt: Long,
    val lastModified: Long
) : BaseDto<FileMetadataEntity> {
    companion object {
        fun FileMetadataEntity.toDto() = FileMetadataDto(
            fileId = fileId.toString(),
            objectKey = objectKey,
            filename = filename,
            contentType = contentType,
            fileSize = fileSize,
            etag = etag,
            owner = owner.toString(),
            isPublic = isPublic,
            createdAt = createdAt.toEpochMilli(),
            lastModified = lastModified.toEpochMilli()
        )
    }

    override fun toEntity(): FileMetadataEntity {
        val entity = FileMetadataEntity()
        entity.fileId = UUID.fromString(fileId)
        entity.objectKey = objectKey
        entity.filename = filename
        entity.contentType = contentType
        entity.fileSize = fileSize
        entity.etag = etag
        entity.owner = UUID.fromString(owner)
        entity.isPublic = isPublic
        entity.createdAt = Instant.ofEpochMilli(createdAt)
        entity.lastModified = Instant.ofEpochMilli(lastModified)
        return entity
    }
}