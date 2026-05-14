import com.github.huymaster.server.api.models.request.DeviceRegisterRequest
import com.github.huymaster.server.api.security.ED25519KeyPairGenerator
import com.github.huymaster.server.api.security.MLKEMKeyPairGenerator
import com.github.huymaster.server.api.utils.DefaultCbor
import com.github.huymaster.server.api.utils.DefaultJson
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToByteArray
import kotlin.test.Test

class TypeTest {
    @Test
    fun test() = runBlocking {
        val request = DeviceRegisterRequest(
            MLKEMKeyPairGenerator.getInstance().generate().first.encoded,
            ED25519KeyPairGenerator.getInstance().generate().first.encoded,
            "System32"
        )
        val cbor = DefaultCbor.encodeToByteArray(request)
        val json = DefaultJson.encodeToString(request)
        println(cbor.size)
        println(json.length)

    }
}