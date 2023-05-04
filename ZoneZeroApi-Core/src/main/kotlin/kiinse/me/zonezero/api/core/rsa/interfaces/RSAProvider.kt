package kiinse.me.zonezero.api.core.rsa.interfaces

import kiinse.me.zonezero.api.core.exceptions.RSAException
import kiinse.me.zonezero.api.core.rsa.RSA
import kiinse.me.zonezero.api.core.rsa.data.EncryptedMessage
import kotlinx.serialization.SerializationStrategy
import java.security.PublicKey

interface RSAProvider {

    @Throws(RSAException::class)
    fun generateKeys()

    @Throws(RSAException::class)
    fun decrypt(encrypted: EncryptedMessage): String

    @Throws(RSAException::class)
    fun decrypt(value: String): String

    fun getPublicKeyString(): String

    @Throws(RSAException::class)
    fun encrypt(string: String, publicKey: PublicKey): EncryptedMessage

    @Throws(RSAException::class)
    fun encryptRsa(string: String, publicKey: PublicKey): String

    @Throws(RSAException::class)
    fun recreatePublicKey(key: String): PublicKey

    companion object {
        fun get(): RSAProvider {
            return RSA()
        }
    }
}