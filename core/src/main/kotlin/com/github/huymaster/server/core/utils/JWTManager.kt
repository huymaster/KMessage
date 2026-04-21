package com.github.huymaster.server.core.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.jwt.*
import java.time.Instant
import java.util.*

object JWTManager {
    sealed class Token {
        data class Access(val userId: String, val createdBy: String) : Token()
        data class Refresh(val userId: String, val ipAddress: String) : Token()
    }

    const val ACCESS_TOKEN_EXPIRATION = 5 * 60 * 1000L
    const val REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L
    private const val TOKEN_TYPE_CLAIM = "type"
    private const val USER_ID_CLAIM = "userId"
    private const val IP_ADDRESS_CLAIM = "ipAddress"
    private const val CREATED_BY_CLAIM = "createdBy"

    private val secret by EnvironmentVariables.JWT_SECRET
    private val realm by EnvironmentVariables.JWT_REALM
    private val audience by EnvironmentVariables.JWT_AUDIENCE
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun getVerifier(): JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(realm)
            .withAudience(audience)
            .build()

    private fun createJWT(isRefresh: Boolean = false, builder: JWTCreator.Builder.() -> Unit): String {
        val now: Instant = Instant.now()
        val expiration = if (isRefresh) REFRESH_TOKEN_EXPIRATION else ACCESS_TOKEN_EXPIRATION
        return JWT.create()
            .withAudience(audience)
            .withIssuer(realm)
            .withJWTId(UUID.randomUUID().toString())
            .withNotBefore(now)
            .withIssuedAt(now)
            .withExpiresAt(now.plusMillis(expiration))
            .apply(builder)
            .sign(algorithm)
    }


    fun validateJWT(credential: JWTCredential): Boolean {
        val validAudience = credential.audience.contains(audience)
        val validIssuer = credential.issuer == realm

        return validAudience && validIssuer
    }

    fun createRefreshToken(
        userId: UUID,
        ipAddress: String
    ) = createRefreshToken(userId.toString(), ipAddress)

    fun createRefreshToken(
        userId: String,
        ipAddress: String
    ): String = createJWT(true) {
        withClaim(TOKEN_TYPE_CLAIM, "refresh")
        withClaim(USER_ID_CLAIM, userId)
        withClaim(IP_ADDRESS_CLAIM, ipAddress)
    }

    fun createAccessToken(userId: UUID, refreshToken: String): String =
        createAccessToken(userId.toString(), refreshToken)

    fun createAccessToken(userId: String, refreshToken: String): String = createJWT {
        withClaim(TOKEN_TYPE_CLAIM, "access")
        withClaim(USER_ID_CLAIM, userId)
        withClaim(CREATED_BY_CLAIM, refreshToken)
    }

    fun extractToken(token: String): Token {
        val verifier = getVerifier()
        val claims = verifier.verify(token).claims
        return when (val type = claims[TOKEN_TYPE_CLAIM]?.asString()) {
            "access" -> {
                val userId = claims[USER_ID_CLAIM]?.asString()
                    ?: throw IllegalArgumentException("User ID not found in token")
                val createdBy = claims[CREATED_BY_CLAIM]?.asString()
                require(createdBy != null) { "Refresh token not found in token" }
                Token.Access(userId, createdBy)
            }

            "refresh" -> {
                val userId = claims[USER_ID_CLAIM]?.asString()
                    ?: throw IllegalArgumentException("User ID not found in token")
                val ipAddress = claims[IP_ADDRESS_CLAIM]?.asString()
                    ?: throw IllegalArgumentException("IP address not found in token")

                Token.Refresh(userId, ipAddress)
            }

            else -> throw IllegalArgumentException("Invalid token type: $type")
        }
    }

    fun extractToken(principal: JWTPrincipal): Token {
        val payload = principal.payload
        return when (val type = payload.getClaim(TOKEN_TYPE_CLAIM).asString()) {
            "access" -> {
                val userId = payload.getClaim(USER_ID_CLAIM).asString()
                val createdBy = payload.getClaim(CREATED_BY_CLAIM).asString()
                Token.Access(userId, createdBy)
            }

            "refresh" -> {
                val userId = payload.getClaim(USER_ID_CLAIM).asString()
                val ipAddress = payload.getClaim(IP_ADDRESS_CLAIM).asString()
                Token.Refresh(userId, ipAddress)
            }

            else -> throw IllegalArgumentException("Invalid token type: $type")
        }
    }

    fun extractRefreshToken(token: String): Token.Refresh {
        val token = extractToken(token)
        require(token is Token.Refresh) { "Token is not refresh token" }
        return token
    }

    fun extractAccessToken(token: String): Token.Access {
        val token = extractToken(token)
        require(token is Token.Access) { "Token is not access token" }
        return token
    }
}