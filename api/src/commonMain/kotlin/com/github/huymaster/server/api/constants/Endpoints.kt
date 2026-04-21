package com.github.huymaster.server.api.constants

object Endpoints {
    const val BASE_SERVICE = ""
    const val BASE_SERVICE_HEALTH = "health"

    const val AUTH_SERVICE = "auth"
    const val AUTH_SERVICE_REGISTER = "register"
    const val AUTH_SERVICE_LOGIN = "login"
    const val AUTH_SERVICE_REFRESH = "refresh"
    const val AUTH_SERVICE_LOGOUT = "logout"

    const val FILE_SERVICE = "files"
    const val FILE_SERVICE_CREATE_UPLOAD = "upload/create"
    const val FILE_SERVICE_UPLOAD_SESSION = "upload/{sessionId}"
    const val FILE_SERVICE_UPLOAD_COMPLETE = "upload/complete"
    const val FILE_SERVICE_INFO = "{fileId}"
    const val FILE_SERVICE_DOWNLOAD = "{fileId}/download"
    const val FILE_SERVICE_DELETE = "{fileId}"

    const val USER_SERVICE = "user"
    const val USER_SERVICE_USER_ID = "me"
    const val USER_SERVICE_INFO = "{userId}"

    const val KEY_SERVICE = "key"
    const val KEY_SERVICE_INIT_DEVICE = "init-device"
    const val KEY_SERVICE_DELETE_DEVICE = "delete-device"
    const val KEY_SERVICE_UPLOAD_SIGNED_PREKEYS = "signed-prekeys"
    const val KEY_SERVICE_UPLOAD_ONE_TIME_PREKEYS = "one-time-prekeys"

    class EndpointConfiguration(var baseUrl: String = "http://localhost:8080") {
        private val paths = mutableListOf<String>()

        operator fun String.unaryPlus() {
            if (this.isNotBlank())
                paths.add(this.trim().trimStart('/').trimEnd('/'))
        }

        fun buildUrl(includeBaseUrl: Boolean = true): String {
            val cleanBase = if (includeBaseUrl) baseUrl.trimEnd('/') else ""
            val combinedPath = paths.joinToString("/")
            return if (combinedPath.isEmpty()) cleanBase else "$cleanBase/$combinedPath"
        }
    }

    fun get(block: EndpointConfiguration.(Endpoints) -> Unit): String =
        EndpointConfiguration().apply { block(Endpoints) }.buildUrl()

    fun get(baseUrl: String, block: EndpointConfiguration.(Endpoints) -> Unit): String =
        EndpointConfiguration(baseUrl).apply { block(Endpoints) }.buildUrl()
}