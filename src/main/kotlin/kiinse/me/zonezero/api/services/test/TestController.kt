package kiinse.me.zonezero.api.services.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.body.DataAnswer
import kiinse.me.zonezero.api.core.body.MessageAnswer
import kiinse.me.zonezero.api.core.config.Addresses
import kiinse.me.zonezero.api.security.ApiRSA
import kiinse.me.zonezero.api.security.annotations.Authentication
import kiinse.me.zonezero.api.core.security.enums.AccountType
import kiinse.me.zonezero.api.services.ServiceUtils
import kiinse.me.zonezero.api.utils.ResponseFactory

@Controller("/test")
open class TestController {

    @Get
    fun index(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        return ResponseFactory.create(request, HttpStatus.OK, MessageAnswer(ApiRSA.get().getPublicKeyString()))
    }

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        return ResponseFactory.create(HttpStatus.valueOf(ServiceUtils.get(request, Addresses.TEST, "status").code))
    }

    @Get("/getTestMessageGet")
    @Authentication(permissions = [AccountType.ALL])
    open fun getTestMessageGet(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.get(request, Addresses.TEST, "getTestMessageGet")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/getTestMessagePost")
    @Authentication(permissions = [AccountType.ALL])
    open fun getTestMessagePost(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.TEST, "getTestMessagePost")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }
}