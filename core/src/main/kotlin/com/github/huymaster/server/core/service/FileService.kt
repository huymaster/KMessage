package com.github.huymaster.server.core.service

import com.github.huymaster.server.core.database.table.FileMetadataTable
import com.github.huymaster.server.core.dto.CreateUploadSessionDto
import com.github.huymaster.server.core.utils.DirectMemoryPool
import com.github.huymaster.server.core.utils.serverSideJson
import io.lettuce.core.json.JsonPath
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.*

class FileService : BaseService() {
    @Serializable
    private data class Session(
        val owner: String,
        val filename: String,
        val contentType: String,
        val fileSize: Long,
        val etag: String,
        val isPublic: Boolean,
        val transfered: Long = 0,
    ) {
        companion object {
            fun from(input: CreateUploadSessionDto) = Session(
                owner = input.owner,
                filename = input.filename,
                contentType = input.contentType,
                fileSize = input.fileSize,
                etag = input.etag,
                isPublic = input.isPublic
            )
        }
    }

    companion object {
        private const val FILE_METADATA_CACHE_KEY = "file:metadata"
        private const val FILE_METADATA_CACHE_EXPIRE_TIME = 5 * 60L
        private const val UPLOAD_SESSION_CACHE_KEY = "file:session"
        private const val UPLOAD_SESSION_CACHE_EXPIRE_TIME = 10 * 60L
    }

    private val pool = DirectMemoryPool.createProvider(16, 64, "FileService")
    private val secure = SecureRandom()
    private val metadatas by injectRepository(FileMetadataTable)

    init {
        minio.initialize("file")
    }

    suspend fun createSession(request: CreateUploadSessionDto): Result<String> = runCatching {
        val sessionId = createNewSessionId()
        val sessionKey = "$UPLOAD_SESSION_CACHE_KEY:$sessionId"
        val session = Session.from(request)

        redis.jsonSet(
            sessionKey,
            JsonPath.ROOT_PATH,
            serverSideJson.encodeToString(session),
        )
        redis.expire(sessionKey, UPLOAD_SESSION_CACHE_EXPIRE_TIME)

        sessionId
    }


    private fun createNewSessionId(): String {
        val uuid = UUID.randomUUID()
        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array())
    }

    private suspend fun getSession(sessionId: String): Session? {
        val sessionKey = "$UPLOAD_SESSION_CACHE_KEY:$sessionId"
        val jsonResults = redis.jsonGet(sessionKey, JsonPath.ROOT_PATH)
        val firstMatch = jsonResults.firstOrNull()?.toString() ?: return null
        return try {
            val sessions: List<Session> = serverSideJson.decodeFromString(firstMatch)
            sessions.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}