package com.github.huymaster.server.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUploadSessionDto(
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val etag: String,
    val owner: String,
    val isPublic: Boolean = false
) {
}