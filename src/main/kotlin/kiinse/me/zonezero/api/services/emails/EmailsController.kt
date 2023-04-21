package kiinse.me.zonezero.api.services.emails

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.config.Addresses
import kiinse.me.zonezero.api.security.ApiRSA
import kiinse.me.zonezero.api.security.annotations.Authentication
import kiinse.me.zonezero.api.security.enums.AccountType
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.services.ServiceUtils
import kiinse.me.zonezero.api.utils.ResponseFactory

@Controller("/email")
open class EmailsController {

    @Get
    fun index(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(request, HttpStatus.OK, ApiRSA.get().getPublicKeyString())
    }

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.valueOf(ServiceUtils.get(request, Addresses.EMAIL, "status").code))
    }

    @Post("/send")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun sendEmail(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.EMAIL, "send")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }
}