import com.github.huymaster.server.api.security.MLKEMKeyPairGenerator
import kotlinx.coroutines.runBlocking
import org.bouncycastle.crypto.kems.MLKEMGenerator
import java.security.SecureRandom
import kotlin.test.Test

class KeyTest {


    @Test
    fun test() = runBlocking {
        val (pub, pri) = MLKEMKeyPairGenerator.getInstance().generate()

        val gen = MLKEMGenerator(SecureRandom())
        val enc = gen.generateEncapsulated(pub)

        println(enc.encapsulation.toHexString())
        println(enc.secret.toHexString())
    }
}