package com.github.huymaster.server.api.models.common

import kotlinx.serialization.Serializable

@Serializable
data class TokenCookie(val token: String)