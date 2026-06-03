package com.github.huymaster.kmessage.di

import com.github.huymaster.kmessage.core.data.source.AuthTokenDataSource
import com.github.huymaster.kmessage.core.utils.SERVER_HOST
import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.utils.DefaultCbor
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.cbor.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.module

object NetworkModule : KoinModuleProvider {
    @OptIn(ExperimentalSerializationApi::class)
    private val httpClientModule = module {
        single<HttpClient> {
            val tokenDataSource = get<AuthTokenDataSource>()
            HttpClient(CIO) {
                defaultRequest {
                    host = SERVER_HOST
                    accept(ContentType.Application.Cbor)
                }
                install(Auth) {
                    bearer {
                        loadTokens {
                            val accessToken = tokenDataSource.accessToken.first()
                            val refreshToken = tokenDataSource.refreshToken.first()

                            if (accessToken != null && refreshToken != null) {
                                BearerTokens(accessToken, refreshToken)
                            } else {
                                null
                            }
                        }

                        refreshTokens {
                            val oldRefreshToken = tokenDataSource.refreshToken.first()
                            if (oldRefreshToken.isNullOrBlank()) return@refreshTokens null

                            try {
                                val tokenClient = HttpClient(CIO) {
                                    defaultRequest {
                                        host = SERVER_HOST
                                    }
                                    install(ContentNegotiation) { cbor(DefaultCbor) }
                                }

                                val response = tokenClient.get(Endpoints.AUTH_SERVICE_REFRESH) {
                                    contentType(ContentType.Application.Cbor)
                                    setBody(oldRefreshToken)
                                }

                                if (response.status == HttpStatusCode.OK) {
                                    val (refreshToken, accessToken) = response.body<Pair<String, String>>()

                                    tokenDataSource.saveAccessToken(accessToken)
                                    tokenDataSource.saveRefreshToken(refreshToken)

                                    BearerTokens(
                                        accessToken = accessToken,
                                        refreshToken = refreshToken
                                    )
                                } else {
                                    tokenDataSource.clearTokens()
                                    null
                                }
                            } catch (_: Exception) {
                                tokenDataSource.clearTokens()
                                null
                            }
                        }

                        sendWithoutRequest { request ->
                            request.host == SERVER_HOST
                        }
                    }
                }
                install(ContentNegotiation) {
                    cbor(DefaultCbor)
                }
                install(WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(DefaultCbor)
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun provide() = setOf(
        httpClientModule
    )
}