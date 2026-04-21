package com.github.huymaster.server.core.service

import com.github.huymaster.server.api.exceptions.ServiceException
import com.github.huymaster.server.api.models.common.UserRole
import com.github.huymaster.server.api.utils.PasswordValidator
import com.github.huymaster.server.api.utils.UsernameValidator
import com.github.huymaster.server.api.utils.onFail
import com.github.huymaster.server.api.utils.validateWith
import com.github.huymaster.server.core.database.table.UserCredentialTable
import com.github.huymaster.server.core.database.table.UserRoleTable
import com.github.huymaster.server.core.database.table.UserTable
import com.github.huymaster.server.core.database.table.UserTokenTable
import com.github.huymaster.server.core.utils.JWTManager
import com.github.huymaster.server.core.utils.PasswordManager
import com.github.huymaster.server.core.utils.UUIDv7
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import org.ktorm.dsl.eq
import java.time.Instant
import java.util.*

@Suppress("OPT_IN_USAGE")
class AuthService : BaseService() {
    private val users by injectRepository(UserTable)
    private val roles by injectRepository(UserRoleTable)
    private val credentials by injectRepository(UserCredentialTable)
    private val tokens by injectRepository(UserTokenTable)

    suspend fun register(
        username: String,
        password: String
    ): Result<Unit> = runCatching {
        username.lowercase().validateWith(UsernameValidator)
            .onFail { serviceException(HttpStatusCode.BadRequest, "register.username_invalid") }
        password.validateWith(PasswordValidator)
            .onFail { serviceException(HttpStatusCode.BadRequest, "register.password_invalid") }

        transactionWithRetryBreaker {
            val isExists = users.exists { it.username eq username }
            if (isExists) serviceException(HttpStatusCode.Conflict, "register.user_exists")
            val userId = UUIDv7.randomUUID().toUUID()
            val hash = PasswordManager.hash(password)

            users.insert {
                set(it.userId, userId)
                set(it.username, username)
            }

            credentials.insert {
                set(it.userId, userId)
                set(it.passkey, hash)
            }

            roles.insert {
                set(it.userId, userId)
                set(it.role, UserRole.USER)
            }
        }.onFailure {
            if (it is ServiceException) throw it
            serviceException(HttpStatusCode.InternalServerError, "register.db_error")
        }.getOrThrow()
    }

    suspend fun login(
        username: String,
        password: String,
        ipAddress: String
    ): Result<String> = runCatching {
        username.lowercase().validateWith(UsernameValidator)
            .onFail { serviceException(HttpStatusCode.BadRequest, "register.username_invalid") }

        transactionWithRetryBreaker {
            val user = users.find { it.username eq username }.firstOrNull()
            val credential = user?.let { u ->
                credentials.find { it.userId eq u.userId }.firstOrNull()
            }

            if (user == null || credential == null || !PasswordManager.verify(password, credential.passkey))
                serviceException(HttpStatusCode.Unauthorized, "login.invalid_credentials")

            val token = JWTManager.createRefreshToken(user.userId, ipAddress)

            val affectedRows = tokens.insert {
                set(it.token, token)
                set(it.expiredAt, Instant.now().plusMillis(JWTManager.REFRESH_TOKEN_EXPIRATION))
                set(it.owner, user.userId)
            }
            if (affectedRows < 1) serviceException(HttpStatusCode.InternalServerError, "login.db_error")

            token
        }.getOrThrow()
    }

    suspend fun refreshToken(
        refreshToken: String,
        ipAddress: String
    ): Result<Pair<String, String>> = runCatching {
        val verifyResult = verifyRefreshToken(refreshToken).getOrThrow()
        if (!verifyResult) serviceException(HttpStatusCode.Unauthorized, "refresh.refresh_token_invalid")
        val extractedToken = JWTManager.extractRefreshToken(refreshToken)

        transactionWithRetryBreaker {
            tokens.update {
                set(it.isRevoked, true)
                where { (it.token eq refreshToken) }
            }

            val newRt = JWTManager.createRefreshToken(extractedToken.userId, ipAddress)
            val newAt = JWTManager.createAccessToken(extractedToken.userId, newRt)
            val createdRows = tokens.insert {
                set(it.token, newRt)
                set(it.expiredAt, Instant.now().plusMillis(JWTManager.REFRESH_TOKEN_EXPIRATION))
                set(it.owner, UUID.fromString(extractedToken.userId))
            }
            if (createdRows < 1) serviceException(HttpStatusCode.InternalServerError, "refresh.db_error")
            Pair(newRt, newAt)
        }.getOrThrow()
    }

    suspend fun logout(
        refreshToken: String
    ): Result<Unit> = runCatching {
        transactionWithRetryBreaker {
            tokens.update {
                set(it.isRevoked, true)
                where { it.token eq refreshToken }
            }
        }.getOrThrow()
    }

    suspend fun verifyRefreshToken(
        refreshToken: String
    ): Result<Boolean> = runCatching {
        val token = JWTManager.extractRefreshToken(refreshToken)

        val isValidUser = users.exists { it.userId eq UUID.fromString(token.userId) }
        val isValidInDb = transactionWithRetryBreaker {
            val tokenRecord = tokens.find { it.token eq refreshToken }.firstOrNull()
            tokenRecord != null && !tokenRecord.isRevoked && tokenRecord.expiresAt > Instant.now()
        }.getOrThrow()

        isValidUser && isValidInDb
    }
}