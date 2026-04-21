package com.github.huymaster.server.core.service

import com.github.huymaster.server.core.database.table.*
import com.github.huymaster.server.core.dto.DeleteDeviceDto
import com.github.huymaster.server.core.dto.InitDeviceDto
import com.github.huymaster.server.core.dto.UploadSignedPrekeyDto
import com.github.huymaster.server.core.utils.UUIDv7
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.ktorm.dsl.*
import java.security.SecureRandom
import java.time.Instant
import java.util.*

class KeyService : BaseService() {
    companion object {
        const val SIGNED_PREKEYS_EXPIRE_TIME = 30 * 24 * 60 * 60 * 1000L
    }

    private val users by injectRepository(UserTable)
    private val devices by injectRepository(UserDeviceTable)
    private val sessions by injectRepository(UserSessionTable)
    private val signedPrekeys by injectRepository(SignedPrekeysTable)
    private val oneTimePrekeys by injectRepository(OneTimePrekeysTable)

    suspend fun cleanUpOneTimeKeys(): Int = transactionWithRetryBreaker {
        oneTimePrekeys.delete { it.isUsed eq true }
    }
        .onFailure { logger.warn("Clean up one time keys failed", it) }
        .onSuccess { logger.debug("Cleaned up {} one time keys", it) }
        .getOrElse { 0 }

    suspend fun cleanUpSignedKeys(): Int = transactionWithRetryBreaker {
        val keys: Query = sessions.query { it.select(signedPrekeysId) }

        signedPrekeys.delete {
            ((it.isActive eq false) or (it.expiredAt lt Instant.now())) and (it.keyId notInList keys)
        }
    }
        .onFailure { logger.warn("Clean up signed keys failed", it) }
        .onSuccess { logger.debug("Cleaned up {} signed keys", it) }
        .getOrElse { 0 }

    suspend fun initDevice(init: InitDeviceDto) = transactionWithRetryBreaker {
        val uId = UUID.fromString(init.userId)
        val dId = UUID.fromString(init.deviceId)

        if (!isEd25519PublicKey(init.identityKey))
            serviceException(HttpStatusCode.BadRequest, "key.invalid_public_key")

        if (devices.exists { (it.userId eq uId) and (it.deviceId eq dId) })
            serviceException(HttpStatusCode.Conflict, "key.device_exists")

        val rId = SecureRandom().apply { setSeed(dId.mostSignificantBits + dId.leastSignificantBits) }
            .nextInt(0, Integer.MAX_VALUE)

        val rowAffected = devices.insert {
            set(it.userId, uId)
            set(it.deviceId, dId)
            set(it.deviceName, init.deviceName)
            set(it.identityKey, init.identityKey)
            set(it.registrationId, rId)
        }
        if (rowAffected > 0) rId else serviceException(HttpStatusCode.InternalServerError, "error.internal")
    }

    suspend fun deleteDevice(delete: DeleteDeviceDto) = transactionWithRetryBreaker {
        val uId = UUID.fromString(delete.userId)
        val dId = UUID.fromString(delete.deviceId)
        val device = devices.find {
            (it.deviceId eq dId) and (it.userId eq uId)
        }.firstOrNull()
            ?: serviceException(HttpStatusCode.NotFound, "key.device_not_found")

        val affectedRow = devices.delete { it.deviceId eq device.deviceId }
        if (affectedRow <= 0) serviceException(HttpStatusCode.InternalServerError, "error.internal")
    }

    suspend fun uploadSignedPrekeys(upload: UploadSignedPrekeyDto) = transactionWithRetryBreaker {
        val dId = UUID.fromString(upload.deviceId)

        val device = devices.find { it.deviceId eq dId }.firstOrNull()
            ?: serviceException(HttpStatusCode.NotFound, "key.device_not_found")

        if (!isEd25519PublicKey(upload.signedPrekey))
            serviceException(HttpStatusCode.BadRequest, "key.invalid_public_key")

        if (!verifySignature(device.identityKey, upload.signedPrekey, upload.signature))
            serviceException(HttpStatusCode.BadRequest, "key.invalid_signature")

        signedPrekeys.update {
            set(it.isActive, false)
            where { (it.deviceId eq dId) and (it.isActive eq true) }
        }

        val kId = UUIDv7.randomUUID().toUUID()
        val rowAffected = signedPrekeys.insert {
            set(it.keyId, kId)
            set(it.deviceId, dId)
            set(it.key, upload.signedPrekey)
            set(it.signature, upload.signature)
            set(it.createdAt, Instant.now())
            set(it.expiredAt, Instant.now().plusMillis(SIGNED_PREKEYS_EXPIRE_TIME))
        }
        if (rowAffected > 0) kId else serviceException(HttpStatusCode.InternalServerError, "error.internal")
    }

    private fun isEd25519PublicKey(pk: ByteArray): Boolean {
        if (pk.size != 32) return false
        return try {
            Ed25519PublicKeyParameters(pk, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun verifySignature(pk: ByteArray, msg: ByteArray, sig: ByteArray): Boolean {
        if (sig.size != 64 || pk.size != 32) return false

        return try {
            val publicKey = Ed25519PublicKeyParameters(pk, 0)
            val signer = Ed25519Signer()

            signer.init(false, publicKey)
            signer.update(msg, 0, msg.size)

            signer.verifySignature(sig)
        } catch (e: Exception) {
            logger.warn("Verification failed due to invalid data format", e)
            false
        }
    }
}