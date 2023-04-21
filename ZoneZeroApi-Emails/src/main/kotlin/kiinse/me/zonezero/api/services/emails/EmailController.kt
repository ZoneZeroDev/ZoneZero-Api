package kiinse.me.zonezero.api.services.emails

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.config.ConfigFactory
import kiinse.me.zonezero.api.email.Email
import kiinse.me.zonezero.api.email.EmailService
import kiinse.me.zonezero.api.email.EmailUtils
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import java.io.File

@Controller("/email")
open class EmailController {

    private val emailService = EmailService(EmailUtils.getEmailSettings(ConfigFactory.config))
    private val templates = HashMap<String, File>()

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Post("/send")
    open fun sendEmail(request: HttpRequest<String?>, @Body email: Email): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            if (email.template != null) {
                val templateName = email.template.name.lowercase()
                if (templates.containsKey(templateName)) {
                    emailService.sendTemplateMail(templates[templateName]!!, email)
                    return@runWithCatch ResponseFactory.create(HttpStatus.OK)
                } else {
                    val template = ConfigFactory.getFile("$templateName.mjml") ?: return@runWithCatch ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Email template '$templateName' not found!")
                    templates[templateName] = template
                    emailService.sendTemplateMail(template, email)
                    return@runWithCatch ResponseFactory.create(HttpStatus.OK)
                }
            }
            return@runWithCatch ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Template is null!")
        }
    }
}