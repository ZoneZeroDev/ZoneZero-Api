package kiinse.me.zonezero.api.core.rsa

import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.exceptions.RSAException
import kiinse.me.zonezero.api.core.rsa.data.EncryptedMessage
import kiinse.me.zonezero.api.core.rsa.enums.KeyType
import kiinse.me.zonezero.api.core.rsa.interfaces.RSAProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class RSA : RSAProvider {

    private val keys: MutableMap<KeyType, Key> = EnumMap(KeyType::class.java)
    private val rsa: String = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private val aes: String = "AES"
    private var publicKeyString: String = String()

    override fun generateKeys() {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            val keyPair = keyPairGenerator.generateKeyPair()
            val privateKey = keyPair.private
            val publicKey = keyPair.public
            keys[KeyType.PRIVATE] = privateKey
            keys[KeyType.PUBLIC] = publicKey
            publicKeyString = Base64.encodeBase64String(publicKey.encoded)
        } catch (e: Exception) {
            throw RSAException(HttpStatus.INTERNAL_SERVER_ERROR, e)
        }
    }

    @Throws(RSAException::class)
    override fun decrypt(encrypted: EncryptedMessage): String = runBlocking {
        try {
            val cipher = Cipher.getInstance(rsa)
            cipher.init(Cipher.DECRYPT_MODE, keys[KeyType.PRIVATE])
            val aesKey = cipher.doFinal(Base64.decodeBase64(encrypted.aes))
            val originalKey = SecretKeySpec(aesKey, 0, aesKey.size, "AES")
            val aesCipher = async {
                val aesCipher = Cipher.getInstance(aes)
                aesCipher.init(Cipher.DECRYPT_MODE, originalKey)
                return@async aesCipher
            }
            val message = async { Base64.decodeBase64(encrypted.message) }
            return@runBlocking String(aesCipher.await().doFinal(message.await()))
        } catch (e: Exception) {
            throw RSAException(HttpStatus.INTERNAL_SERVER_ERROR, e)
        }
    }

    @Throws(RSAException::class)
    override fun decrypt(value: String): String = runBlocking {
        try {
            val cipher = Cipher.getInstance(rsa)
            cipher.init(Cipher.DECRYPT_MODE, keys[KeyType.PRIVATE])
            return@runBlocking String(cipher.doFinal(Base64.decodeBase64(value)))
        } catch (e: Exception) {
            throw RSAException(HttpStatus.INTERNAL_SERVER_ERROR, e)
        }
    }

    override fun getPublicKeyString(): String {
        return publicKeyString
    }

    override fun encrypt(string: String, publicKey: PublicKey): EncryptedMessage = runBlocking {
        val generator = async {
            val generator = KeyGenerator.getInstance(aes)
            generator.init(128)
            return@async generator
        }
        val aesKey: SecretKey = generator.await().generateKey()
        val aesCipher = async {
            val cipher = Cipher.getInstance(aes)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey)
            return@async cipher
        }
        val cipher = async {
            val cipher = Cipher.getInstance(rsa)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return@async cipher
        }
        val aes = async { Base64.encodeBase64String(cipher.await().doFinal(aesKey.encoded)) }
        val message = async { Base64.encodeBase64String(aesCipher.await().doFinal(string.toByteArray())) }
        return@runBlocking EncryptedMessage(aes.await(), message.await())
    }

    @Throws(RSAException::class)
    override fun encryptRsa(string: String, publicKey: PublicKey): String = runBlocking {
        val cipher = async {
            val cipher = Cipher.getInstance(rsa)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return@async cipher
        }
        val message = async { string.toByteArray() }
        return@runBlocking Base64.encodeBase64String(cipher.await().doFinal(message.await()))
    }

    @Throws(RSAException::class)
    override fun recreatePublicKey(key: String): PublicKey {
        return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.decodeBase64(key)))
    }
}