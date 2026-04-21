package com.github.huymaster.server.core.database.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface FileMetadataEntity : Entity<FileMetadataEntity> {
    companion object : Entity.Factory<FileMetadataEntity>()

    var fileId: UUID
    var objectKey: String
    var filename: String
    var contentType: String
    var fileSize: Long
    var etag: String
    var owner: UUID
    var isPublic: Boolean
    var createdAt: Instant
    var lastModified: Instant
}