package com.github.huymaster.server.api.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateUploadSessionRequest(
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val etag: String,
    val isPublic: Boolean = false
)