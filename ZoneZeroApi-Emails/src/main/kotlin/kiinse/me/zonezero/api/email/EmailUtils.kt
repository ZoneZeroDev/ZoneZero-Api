package kiinse.me.zonezero.api.email

import org.tomlj.TomlParseResult

object EmailUtils {

    fun getEmailSettings(config: TomlParseResult): EmailServiceSettings {
        val email = config.getTableOrEmpty("email")
        val mjml = config.getTableOrEmpty("mjml")
        return EmailServiceSettings(
            host = email["host"].toString(),
            from = email["from"].toString(),
            port = email["port"].toString().toInt(),
            username = email["username"].toString(),
            password = email["password"].toString(),
            isAuth = email["auth"].toString().toBoolean(),
            isSSl = email["ssl"].toString().toBoolean(),
            sslTrust = email["host"].toString(),
            isStartTTLs = email["starttls"].toString().toBoolean(),
            sslProtocol = email["protocol"].toString(),
            isDebug = email["debug"].toString().toBoolean(),
            mjmlId = mjml["id"].toString(),
            mjmlSecret = mjml["secret"].toString()
        )
    }
}