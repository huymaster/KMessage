package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.core.module.APP_DOMAIN
import com.github.huymaster.server.core.service.FileService
import com.github.huymaster.server.core.utils.getUserIdAsUUID
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.core.component.inject
import java.util.*

object FileRoutes : BaseRoute() {
    override val endpoint: String
        get() = Endpoints.FILE_SERVICE

    private val fileService by inject<FileService>()

    override fun Route.registerRoutes() {
        authenticate(APP_DOMAIN) {
            post(Endpoints.FILE_SERVICE_CREATE_UPLOAD) {
                val userId = getUserIdAsUUID() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                createUpload(userId)
            }
            patch(Endpoints.FILE_SERVICE_UPLOAD_SESSION) {
                val userId = getUserIdAsUUID() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                uploadSession(userId)
            }
            put(Endpoints.FILE_SERVICE_UPLOAD_SESSION) {
                val userId = getUserIdAsUUID() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                uploadSession(userId)
            }
            post(Endpoints.FILE_SERVICE_UPLOAD_COMPLETE) {
                val userId = getUserIdAsUUID() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                uploadComplete(userId)
            }
            delete(Endpoints.FILE_SERVICE_DELETE) {
                val userId = getUserIdAsUUID() ?: serviceException(HttpStatusCode.Unauthorized, "error.unauthorized")
                delete(userId)
            }
        }
        authenticate(APP_DOMAIN, optional = true) {
            get(Endpoints.FILE_SERVICE_INFO) {
                info(getUserIdAsUUID())
            }
            get(Endpoints.FILE_SERVICE_DOWNLOAD) {
                download(getUserIdAsUUID())
            }
        }
    }

    private suspend fun RoutingContext.createUpload(userId: UUID?) {
    }

    private suspend fun RoutingContext.uploadSession(userId: UUID?) {
    }

    private suspend fun RoutingContext.uploadComplete(userId: UUID?) {
    }

    private suspend fun RoutingContext.delete(userId: UUID?) {
    }

    private suspend fun RoutingContext.info(userId: UUID?) {
    }

    private suspend fun RoutingContext.download(userId: UUID?) {
    }
}