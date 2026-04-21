package com.github.huymaster.server.core.net

import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.request.CreateUploadSessionRequest
import com.github.huymaster.server.core.dto.CreateUploadSessionDto
import com.github.huymaster.server.core.module.APP_DOMAIN
import com.github.huymaster.server.core.service.FileService
import com.github.huymaster.server.core.utils.getUserIdAsUUID
import com.github.huymaster.server.core.utils.requestOrThrow
import com.github.huymaster.server.core.utils.serviceException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
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
        val request = call.requestOrThrow<CreateUploadSessionRequest>()
        val dto = CreateUploadSessionDto(
            filename = request.filename,
            contentType = request.contentType,
            fileSize = request.fileSize,
            etag = request.etag,
            owner = userId.toString(),
            isPublic = request.isPublic
        )
        val sessionId = fileService.createSession(dto).getOrThrow()
        call.respond(HttpStatusCode.Created, sessionId)
    }

    private suspend fun RoutingContext.uploadSession(userId: UUID?) {
        val sessionId =
            call.parameters["sessionId"] ?: serviceException(HttpStatusCode.BadRequest, "error.request_invalid")
        val rangeHeader =
            call.request.headers["Range"] ?: serviceException(HttpStatusCode.BadRequest, "error.request_invalid")
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