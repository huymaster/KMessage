package com.github.huymaster.kmessage.core.data.repository

import com.github.huymaster.kmessage.core.data.source.AuthRemoteDataSource
import com.github.huymaster.kmessage.core.data.source.AuthTokenDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.EmptyCoroutineContext

sealed interface AuthState {
    object Initial : AuthState
    object Authenticated : AuthState
    object Unauthenticated : AuthState
}

interface AuthRepository {
    val authState: StateFlow<AuthState>
    fun getState(): AuthState
    suspend fun login(username: String, password: String)
    suspend fun logout()
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
}

class AuthRepositoryImpl(
    private val authTokenDataSource: AuthTokenDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    externalScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
) : AuthRepository {
    override val authState: StateFlow<AuthState> = authTokenDataSource.accessToken
        .map { token ->
            if (token.isNullOrBlank()) AuthState.Unauthenticated else AuthState.Authenticated
        }.stateIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Initial
        )

    override fun getState(): AuthState = authState.value

    override suspend fun login(username: String, password: String) {
        val initToken = authRemoteDataSource.login(username, password)
        val (refreshToken, accessToken) = authRemoteDataSource.refresh(initToken)

        authTokenDataSource.saveAccessToken(accessToken)
        authTokenDataSource.saveRefreshToken(refreshToken)
    }

    override suspend fun logout() {
        authRemoteDataSource.logout()
    }

    override suspend fun getAccessToken() =
        authTokenDataSource.accessToken.first()

    override suspend fun getRefreshToken() =
        authTokenDataSource.refreshToken.first()
}