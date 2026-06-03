package com.github.huymaster.kmessage.core.data.source

import com.github.huymaster.kmessage.core.utils.SERVER_HOST
import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.exceptions.ServiceException
import com.github.huymaster.server.api.models.request.LoginRequest
import com.github.huymaster.server.api.models.request.RegisterRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

interface AuthRemoteDataSource {
    suspend fun register(username: String, password: String): Boolean
    suspend fun login(username: String, password: String): String
    suspend fun refresh(refreshToken: String): Pair<String, String>
    suspend fun logout()
}

class AuthRemoteDataSourceImpl(private val httpClient: HttpClient) : AuthRemoteDataSource {
    private val provider = Endpoints.getProvider(SERVER_HOST)

    override suspend fun register(username: String, password: String): Boolean {
        val request = RegisterRequest(username, password)
        val result = httpClient.post(provider.get(Endpoints.AUTH_SERVICE_REGISTER)) {
            contentType(ContentType.Application.Cbor)
            setBody(request)
        }
        return result == HttpStatusCode.Created
    }

    override suspend fun login(username: String, password: String): String {
        val request = LoginRequest(username, password)
        val result = httpClient.post(provider.get(Endpoints.AUTH_SERVICE_LOGIN)) {
            contentType(ContentType.Application.Cbor)
            setBody(request)
        }
        if (result != HttpStatusCode.OK)
            throw ServiceException(result.status.value, result.bodyAsText())
        return result.bodyAsText()
    }

    override suspend fun refresh(refreshToken: String): Pair<String, String> {
        val result = httpClient.get(provider.get(Endpoints.AUTH_SERVICE_REFRESH)) {
            contentType(ContentType.Application.Cbor)
            setBody(refreshToken)
        }
        if (result != HttpStatusCode.OK)
            throw ServiceException(result.status.value, result.bodyAsText())

        return result.body<Pair<String, String>>()
    }

    override suspend fun logout() {
        httpClient.delete(provider.get(Endpoints.AUTH_SERVICE_LOGIN))
    }
}