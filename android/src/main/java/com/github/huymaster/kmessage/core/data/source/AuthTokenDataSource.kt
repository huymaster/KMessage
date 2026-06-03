package com.github.huymaster.kmessage.core.data.source

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.huymaster.kmessage.core.utils.authDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AuthTokenDataSource {
    val accessToken: Flow<String?>
    val refreshToken: Flow<String?>

    suspend fun saveAccessToken(token: String)
    suspend fun saveRefreshToken(token: String)

    suspend fun clearTokens()
}

class AuthTokenDataSourceImpl(context: Context) : AuthTokenDataSource {
    private val store = context.authDataStore

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    override val accessToken: Flow<String?> = store.data.map { it[PreferencesKeys.ACCESS_TOKEN] }
    override val refreshToken: Flow<String?> = store.data.map { it[PreferencesKeys.REFRESH_TOKEN] }

    override suspend fun saveAccessToken(token: String) {
        store.edit { preferences -> preferences[PreferencesKeys.ACCESS_TOKEN] = token }
    }

    override suspend fun saveRefreshToken(token: String) {
        store.edit { preferences -> preferences[PreferencesKeys.REFRESH_TOKEN] = token }
    }

    override suspend fun clearTokens() {
        store.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCESS_TOKEN)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN)
        }
    }
}