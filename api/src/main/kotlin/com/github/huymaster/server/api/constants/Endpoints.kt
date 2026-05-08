package com.github.huymaster.server.api.constants

object Endpoints {
    const val BASE_SERVICE_HEALTH = "health"

    const val AUTH_SERVICE = "auth"
    const val AUTH_SERVICE_REGISTER = "${AUTH_SERVICE}/register"
    const val AUTH_SERVICE_LOGIN = "${AUTH_SERVICE}/login"
    const val AUTH_SERVICE_REFRESH = "${AUTH_SERVICE}/refresh"
    const val AUTH_SERVICE_LOGOUT = "${AUTH_SERVICE}/logout"

    const val FILE_SERVICE = "files"
    const val FILE_SERVICE_CREATE_UPLOAD = "${FILE_SERVICE}/upload/create"
    const val FILE_SERVICE_UPLOAD_SESSION = "${FILE_SERVICE}/upload/{sessionId}"
    const val FILE_SERVICE_UPLOAD_COMPLETE = "${FILE_SERVICE}/upload/complete"
    const val FILE_SERVICE_INFO = "${FILE_SERVICE}/{fileId}"
    const val FILE_SERVICE_DOWNLOAD = "${FILE_SERVICE}/{fileId}/download"
    const val FILE_SERVICE_DELETE = "${FILE_SERVICE}/{fileId}"

    const val USER_SERVICE = "user"
    const val USER_SERVICE_USER_ID = "${USER_SERVICE}/me"
    const val USER_SERVICE_INFO = "${USER_SERVICE}/{userId}"

    const val KEY_SERVICE = "key"

    class EndpointConfiguration(var baseUrl: String = "http://localhost:8080") {
        var path: String = ""

        fun buildUrl(includeBaseUrl: Boolean = true): String {
            val cleanBase = if (includeBaseUrl) baseUrl.trimEnd('/') else ""
            return if (path.isEmpty()) cleanBase else "$cleanBase/$path"
        }
    }

    fun get(block: EndpointConfiguration.(Endpoints) -> Unit): String =
        EndpointConfiguration().apply { block(Endpoints) }.buildUrl()

    fun get(path: String): String =
        EndpointConfiguration().apply { this.path = path }.buildUrl()

    fun get(baseUrl: String, block: EndpointConfiguration.(Endpoints) -> Unit): String =
        EndpointConfiguration(baseUrl).apply { block(Endpoints) }.buildUrl()

    fun get(baseUrl: String, path: String): String =
        EndpointConfiguration(baseUrl).apply { this.path = path }.buildUrl()
}