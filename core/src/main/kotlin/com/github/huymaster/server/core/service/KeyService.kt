package com.github.huymaster.server.core.service

import com.github.huymaster.server.api.models.common.KeyBundle
import com.github.huymaster.server.core.database.table.UserDeviceTable
import com.github.huymaster.server.core.database.table.UserTable
import com.github.huymaster.server.core.utils.UUIDv7
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters
import org.ktorm.dsl.eq
import java.security.SecureRandom
import java.util.*

class KeyService : BaseService() {
    companion object {
    }

    private val users by injectRepository(UserTable)
    private val devices by injectRepository(UserDeviceTable)

    suspend fun registerDevice(
        userId: UUID,
        mlkemPublicKey: ByteArray,
        edPublicKey: ByteArray,
        deviceName: String? = null
    ) = transactionWithRetryBreaker {
        if (!users.exists { it.userId eq userId })
            serviceException(HttpStatusCode.BadRequest, "key.user_not_exists")

        val (isValidMlkem, isValidEd) = verifyKeys(mlkemPublicKey, edPublicKey)

        if (!isValidMlkem)
            serviceException(HttpStatusCode.BadRequest, "key.mlkem_invalid")

        if (!isValidEd)
            serviceException(HttpStatusCode.BadRequest, "key.ed_invalid")

        val deviceId = UUIDv7.randomUUID().toUUID()
        val registrationId = SecureRandom().nextInt()
        devices.insert {
            set(it.userId, userId)
            set(it.deviceId, deviceId)
            set(it.deviceName, deviceName)
            set(it.mlkemPublicKey, mlkemPublicKey)
            set(it.edPublicKey, edPublicKey)
            set(it.registrationId, registrationId)
        }
        registrationId
    }

    suspend fun getKeyBundle(userId: UUID) = transactionWithRetryBreaker {
        val user = users.find { it.userId eq userId }.firstOrNull()
            ?: serviceException(HttpStatusCode.NotFound, "key.user_not_exists")

        val devices = devices.find { it.userId eq user.userId }
        val keys = devices.map { device ->
            device.mlkemPublicKey to device.edPublicKey
        }
        val mlkemKeys = keys.map { it.first }
        val edKeys = keys.map { it.second }
        KeyBundle(mlkemKeys, edKeys)
    }

    private fun verifyKeys(
        mlkemPublicKey: ByteArray,
        edPublicKey: ByteArray
    ): Pair<Boolean, Boolean> {
        val isValidMlkem = runCatching {
            MLKEMPublicKeyParameters(MLKEMParameters.ml_kem_768, mlkemPublicKey)
            true
        }.getOrDefault(false)

        val isValidEd = runCatching {
            Ed25519PublicKeyParameters(edPublicKey)
            true
        }.getOrDefault(false)

        return Pair(isValidMlkem, isValidEd)
    }
}