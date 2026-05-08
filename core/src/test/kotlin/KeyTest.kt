import com.github.huymaster.server.api.security.KeyEncapsulationService
import kotlinx.coroutines.runBlocking
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.generators.MLKEMKeyPairGenerator
import org.bouncycastle.crypto.kems.MLKEMExtractor
import org.bouncycastle.crypto.kems.MLKEMGenerator
import org.bouncycastle.crypto.params.*
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.security.SecureRandom
import java.security.Security
import kotlin.test.Test

class KeyTest {
    class MLKEMEncapsulationService : KeyEncapsulationService {
        companion object {
            private val PARAMS = MLKEMParameters.ml_kem_768
        }

        private val secureRandom = SecureRandom()

        init {
            Security.addProvider(BouncyCastlePQCProvider())
        }

        override fun generateSecret(publicKey: ByteArray): Pair<ByteArray, ByteArray> {
            val pubKey = MLKEMPublicKeyParameters(PARAMS, publicKey)
            val generator = MLKEMGenerator(secureRandom)
            val enc = generator.generateEncapsulated(pubKey)
            return Pair(enc.secret, enc.encapsulation)
        }

        override fun decapsulate(ciphertext: ByteArray, privateKey: ByteArray): ByteArray {
            val priKey = MLKEMPrivateKeyParameters(PARAMS, privateKey)
            val extractor = MLKEMExtractor(priKey)
            return extractor.extractSecret(ciphertext)
        }
    }

    @Test
    fun test() = runBlocking {
        val gen = MLKEMKeyPairGenerator()
        gen.init(MLKEMKeyGenerationParameters(SecureRandom(), MLKEMParameters.ml_kem_768))
        val pair = gen.generateKeyPair()
        val pub = pair.public as MLKEMPublicKeyParameters
        val priv = pair.private as MLKEMPrivateKeyParameters

        val service = MLKEMEncapsulationService()

        val (originalSecret, encapsulation) = service.generateSecret(pub.encoded)
        println("Original Secret: ${originalSecret.toHexString()}")

        val recoveredSecret = service.decapsulate(encapsulation, priv.encoded)
        println("Recovered Secret: ${recoveredSecret.toHexString()}")

        assert(recoveredSecret.contentEquals(originalSecret)) {
            "Shared secrets do not match!"
        }

        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(originalSecret, "AES-GCM-256".toByteArray(), "msg_key".toByteArray()))

        val finalKey = ByteArray(32)
        hkdf.generateBytes(finalKey, 0, finalKey.size)
        println("Final Key: ${finalKey.toHexString()}")
    }
}