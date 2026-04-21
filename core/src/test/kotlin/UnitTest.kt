import com.github.huymaster.server.api.constants.Endpoints
import com.github.huymaster.server.api.models.request.CreateUploadSessionRequest
import com.github.huymaster.server.api.models.request.LoginRequest
import com.github.huymaster.server.api.models.request.RegisterRequest
import com.github.huymaster.server.core.utils.serverSideJson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.security.Security
import kotlin.test.Test
import kotlin.test.assertEquals

private sealed interface Msg
private data class Get(val url: Url, val reply: CompletableDeferred<List<Cookie>>) : Msg
private data class Add(val url: Url, val cookie: Cookie) : Msg

private class Storage(private val file: File) : CookiesStorage {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logger: Logger = LoggerFactory.getLogger(Storage::class.java)
    private var initialized = false
    private val cache = mutableListOf<Cookie>()

    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    private val actor = scope.actor<Msg>(capacity = Channel.BUFFERED) {
        initCache()
        for (msg in channel) {
            try {
                when (msg) {
                    is Get -> {
                        val matchedCookies = cache.filter { checkCookie(it, msg.url) }
                        msg.reply.complete(matchedCookies)
                    }

                    is Add -> {
                        cache.removeAll { it.name == msg.cookie.name && it.domain == msg.cookie.domain && it.path == msg.cookie.path }
                        cache.add(msg.cookie)
                        cache.removeAll { it.value.isBlank() }
                        saveToFile()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing actor message", e)
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val reply = CompletableDeferred<List<Cookie>>()
        actor.send(Get(requestUrl, reply))
        return reply.await()
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        actor.send(Add(requestUrl, cookie))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun initCache() {
        if (file.exists()) {
            try {
                file.inputStream().use { cache.addAll(serverSideJson.decodeFromStream(it)) }
                cache.removeAll { it.value.isBlank() }
                saveToFile()
                initialized = true
            } catch (e: Exception) {
                logger.error("Failed to load cookies from file", e)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun saveToFile() {
        try {
            file.outputStream().use { serverSideJson.encodeToStream(cache, it) }
        } catch (e: Exception) {
            logger.error("Failed to save cookies to file", e)
        }
    }

    private fun checkCookie(cookie: Cookie, requestUrl: Url): Boolean {
        val requestHost = requestUrl.host.removePrefix(".")
        val cookieDomain = cookie.domain?.removePrefix(".")

        val domainMatch = if (cookieDomain == null)
            requestHost == requestUrl.host
        else
            requestHost == cookieDomain || requestHost.endsWith(".$cookieDomain")


        val cookiePath = cookie.path?.let { if (it.startsWith("/")) it else "/$it" } ?: "/"
        val requestPath = requestUrl.encodedPath
        val pathMatch = requestPath.startsWith(cookiePath)

        return domainMatch && pathMatch
    }

    override fun close() {
        actor.close()
        scope.cancel()
    }
}

class UnitTest {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UnitTest::class.java)

        @JvmStatic
        @BeforeAll
        fun init() {
            Security.addProvider(BouncyCastleProvider())
            logger.info("initialized")
        }

        val client = HttpClient(CIO) {
            defaultRequest {
                url("http://localhost:8080")
                header(HttpHeaders.AcceptLanguage, "vi;q=0.9,en;q=0.8")
            }
            install(ContentNegotiation) {
                json(serverSideJson)
            }
            install(HttpCookies) {
                storage = Storage(File("cookies.json"))
            }
        }
    }

    @Test
    fun test(): Unit = runBlocking {
        suspend()
    }

    suspend fun suspend() {
        val register = RegisterRequest("huymaster", "Aa123456")
        val login = LoginRequest("huymaster", "Aa123456")
        var result: HttpResponse

        result = client.post(Endpoints.get {
            +it.AUTH_SERVICE
            +it.AUTH_SERVICE_REGISTER
        }) {
            contentType(ContentType.Application.Json)
            setBody(register)
        }

        if (result.status != HttpStatusCode.Created)
            logger.warn("skipped register [{}]: {}", result.status, result.bodyAsText())
        else
            logger.info("register success: {}", result.bodyAsText())

        result = client.post(Endpoints.get {
            +it.AUTH_SERVICE
            +it.AUTH_SERVICE_LOGIN
        }) {
            contentType(ContentType.Application.Json)
            setBody(login)
        }

        if (result.status != HttpStatusCode.OK)
            logger.warn("skipped login [{}]: {}", result.status, result.bodyAsText())
        else
            logger.info("login success: {}", result.bodyAsText())


        result = client.get(Endpoints.get {
            +it.AUTH_SERVICE
            +it.AUTH_SERVICE_REFRESH
        })

        assertEquals(result.status, HttpStatusCode.OK, "refresh token failed: ${result.bodyAsText()}")
        val token = result.bodyAsText()

        val createSession = CreateUploadSessionRequest("test.txt", "text/plain", 1024, "1234567890")
        result = client.post(Endpoints.get {
            +it.FILE_SERVICE
            +it.FILE_SERVICE_CREATE_UPLOAD
        }) {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(createSession)
        }
        assertEquals(result.status, HttpStatusCode.Created, "create upload session failed: ${result.bodyAsText()}")
        logger.info("create upload session success: {}", result.bodyAsText())
    }
}