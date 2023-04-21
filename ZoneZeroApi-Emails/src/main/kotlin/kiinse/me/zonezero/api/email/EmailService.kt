package kiinse.me.zonezero.api.email

import es.atrujillo.mjml.config.template.TemplateFactory
import es.atrujillo.mjml.service.auth.MjmlAuthFactory
import es.atrujillo.mjml.service.definition.MjmlService
import es.atrujillo.mjml.service.impl.MjmlRestService
import io.micronaut.http.HttpStatus
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kiinse.me.zonezero.api.core.exceptions.EmailException
import org.thymeleaf.context.Context
import java.io.File
import java.util.*


class EmailService(private val settings: EmailServiceSettings) {

    private val props = Properties()
    private val mjmlService: MjmlService
    private val session: Session

    init {
        props["mail.smtp.host"] = settings.host
        props["mail.smtp.port"] = settings.port
        props["mail.smtp.auth"] = settings.isAuth
        props["mail.smtp.ssl.enable"] = settings.isSSl
        props["mail.smtp.ssl.trust"] = settings.host
        props["mail.smtp.starttls.enable"] = settings.isStartTTLs
        props["mail.smtp.ssl.protocols"] = settings.sslProtocol
        props["mail.debug"] = settings.isDebug
        session = Session.getDefaultInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(settings.username, settings.password)
            }
        })
        session.debug = settings.isDebug
        mjmlService = MjmlRestService(MjmlAuthFactory.builder()
                                      .withMemoryCredentials()
                                      .mjmlCredentials(settings.mjmlId, settings.mjmlSecret)
                                      .build())
    }

    @Throws(EmailException::class)
    fun sendTemplateMail(template: File, email: Email) {
        try {
            val contextVars = Context()
            email.template!!.data.forEach { (key, value) ->
                contextVars.setVariable(key, value)
            }
            val message = getTemplateMessage(email, mjmlService.transpileMjmlToHtml(TemplateFactory.builder()
                                                                                        .withFileTemplate()
                                                                                        .template(template)
                                                                                        .templateContext(contextVars)
                                                                                        .buildTemplate()))
            val smtpTransport = session.getTransport("smtp")
            smtpTransport.connect()
            smtpTransport.sendMessage(message, message.allRecipients)
            smtpTransport.close()
        } catch (e: Exception) {
            throw EmailException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    private fun getTemplateMessage(email: Email, message: String): MimeMessage {
        val mimeMessage = MimeMessage(session)
        mimeMessage.setFrom(InternetAddress(settings.from))
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.address, false))
        mimeMessage.setContent(message, "text/html; charset=utf-8")
        mimeMessage.subject = email.subject
        mimeMessage.sentDate = Date()
        return mimeMessage
    }
}