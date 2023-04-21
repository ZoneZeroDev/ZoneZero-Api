package kiinse.me.zonezero.api.services.players

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

@Controller("/players")
open class PlayersController {

    @Get
    fun index(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(request, HttpStatus.OK, ApiRSA.get().getPublicKeyString())
    }

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.valueOf(ServiceUtils.get(request, Addresses.PLAYER, "status").code))
    }

    @Post("/login/standard")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun loginPlayerStandard(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "login/standard")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/login/ip")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun loginPlayerIp(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "login/ip")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/register")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun registerPlayer(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "register")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/changePassword")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun changePassword(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "changePassword")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/2fa/enable")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun twoFaEnable(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "2fa/enable")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/2fa/disable")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun twoFaDisable(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "2fa/disable")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/2fa/code")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun twoFaCode(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "2fa/code")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }

    @Post("/check")
    @Authentication(permissions = [AccountType.SERVER_100, AccountType.SERVER_500, AccountType.SERVER_1000])
    open fun checkPlayer(request: HttpRequest<String?>): HttpResponse<Response> {
        val response = ServiceUtils.post(request, Addresses.PLAYER, "check")
        return ResponseFactory.create(request, HttpStatus.valueOf(response.code), response.body)
    }
}