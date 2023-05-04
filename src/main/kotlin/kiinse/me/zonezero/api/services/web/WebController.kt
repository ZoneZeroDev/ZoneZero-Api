package kiinse.me.zonezero.api.services.web

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

@Controller("/web")
open class WebController {

    @Get
    fun index(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        return ResponseFactory.create(request, HttpStatus.OK, MessageAnswer(ApiRSA.get().getPublicKeyString()))
    }

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        return ResponseFactory.create(HttpStatus.valueOf(ServiceUtils.get(request, Addresses.WEB, "status").code))
    }

    @Post("/getServerByCode")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun getServerByCode(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "getServerByCode")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/registerUser")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun registerUser(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "registerUser")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/authUser")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun authUser(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "authUser")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/registerServer")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun registerServer(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "registerServer")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/getUserServers")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun getUserServers(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "getUserServers")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/getServerInfo")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun getServerInfo(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "getServerInfo")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/regenToken")
    @Authentication(permissions = [AccountType.ADMIN])
    open fun regenToken(request: HttpRequest<String?>): HttpResponse<DataAnswer> {
        val response = ServiceUtils.post(request, Addresses.WEB, "regenToken")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }
}