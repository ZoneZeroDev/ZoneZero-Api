package kiinse.me.zonezero.api.security

import kiinse.me.zonezero.api.core.rsa.interfaces.RSAProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ApiRSA {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val rsa: RSAProvider = RSAProvider.get()

    init {
        rsa.generateKeys()
        logger.info("RSA keys generated!")
    }

    fun get(): RSAProvider {
        return rsa
    }
}