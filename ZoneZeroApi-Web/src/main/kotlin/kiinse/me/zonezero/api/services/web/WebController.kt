package kiinse.me.zonezero.api.services.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.core.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.core.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.core.security.Account
import kiinse.me.zonezero.api.core.security.enums.AccountType
import kiinse.me.zonezero.api.core.server.QueryServer
import kiinse.me.zonezero.api.core.server.RegisteredServer
import kiinse.me.zonezero.api.core.server.RegisteredServersList
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import kiinse.me.zonezero.api.core.utils.Utils
import kiinse.me.zonezero.api.services.body.*
import java.time.Instant
import java.util.concurrent.TimeUnit

@Controller("/web")
open class WebController {

    private val serverNotFound = "Server not found!"
    private val userNotFound = "User not found!"
    private val passwordMismatch = "Password mismatch!"
    private val accounts = AccountQuery
    private val queryServers = QueryServerQuery
    private val registeredServers = RegisteredServerQuery

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<String> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Post("/getServerByCode")
    open fun getServerByCode(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, GetServerByCode.serializer()) { body ->
            val server = queryServers.getServer(body.code) ?: return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, serverNotFound)
            return@runOnBody ResponseFactory.create(HttpStatus.OK, QueryServer.serializer(), server)
        }
    }

    @Post("/registerUser")
    open fun registerUser(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, UserRegister.serializer()) { body ->
            if (accounts.hasLogin(body.username)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Username already exists!")
            if (accounts.hasEmail(body.email)) return@runOnBody  ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Email already exists!")
            if (body.password.length < 8) {
                return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Password size cannot be less than 8")
            }
            val jwt = JWT.create()
                .withSubject(body.username)
                .withExpiresAt(Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(1000000000)))
                .sign(Algorithm.HMAC512(body.username + body.password))
            accounts.createAccount(Account(body.username, body.email,
                                           Utils.bcryptHash(body.password), jwt, AccountType.SERVER_1000))
            return@runOnBody ResponseFactory.create(HttpStatus.OK, jwt)
        }
    }

    @Post("/authUser")
    open fun authUser(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, UserAuth.serializer()) { body ->
            if (body.username != null) {
                if (!accounts.hasLogin(body.username)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, userNotFound)
                val account = accounts.getAccountByNamePass(body.username, body.password)
                if (account == null || !account.checkPassword(body.password)) {
                    return@runOnBody ResponseFactory.create(HttpStatus.FORBIDDEN, passwordMismatch)
                }
                return@runOnBody ResponseFactory.create(HttpStatus.OK, Account.serializer(), account)
            } else {
                if (!accounts.hasEmail(body.email!!)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, userNotFound)
                val account = Account.valueOf(body.email, body.password) ?: return@runOnBody ResponseFactory.create(HttpStatus.FORBIDDEN, passwordMismatch)
                return@runOnBody ResponseFactory.create(HttpStatus.OK, Account.serializer(), account)
            }
        }
    }

    @Post("/registerServer")
    open fun registerServer(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, ServerRegister.serializer()) { body ->
            val queryServer = queryServers.getServer(body.code) ?: return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, serverNotFound)
            if (registeredServers.hasServer(queryServer)) {
                return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Server already registered!")
            }
            if (!accounts.hasEmail(body.email)) {
                return@runOnBody ResponseFactory.create(HttpStatus.FORBIDDEN, userNotFound)
            }
            registeredServers.saveServer(queryServer, body.email)
            return@runOnBody ResponseFactory.create(HttpStatus.OK)
        }
    }

    @Post("/getUserServers")
    open fun getUserServers(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, UserServers.serializer()) { body ->
            if (body.email != null) {
                return@runOnBody ResponseFactory.create(HttpStatus.OK, RegisteredServersList.serializer(), registeredServers.getServersByEmail(body.email))
            }
            val account = accounts.getAccountByName(body.username!!) ?: return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, userNotFound)
            return@runOnBody ResponseFactory.create(HttpStatus.OK, RegisteredServersList.serializer(), registeredServers.getServersByEmail(account.email))
        }
    }

    @Post("/getServerInfo")
    open fun getServerInfo(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, GetServerInfo.serializer()) { body ->
            val server = registeredServers.getServer(body.serverId) ?: return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, serverNotFound)
            return@runOnBody ResponseFactory.create(HttpStatus.OK, RegisteredServer.serializer(), server)
        }
    }

    @Post("/regenToken")
    open fun regenToken(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, RegenToken.serializer()) { body ->
            if (!accounts.hasLogin(body.username)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, userNotFound)
            val account = accounts.getAccountByNamePass(body.username, body.password) ?: return@runOnBody ResponseFactory.create(HttpStatus.FORBIDDEN, passwordMismatch)
            val jwt = JWT.create()
                .withSubject(body.username)
                .withExpiresAt(Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(1000000000)))
                .sign(Algorithm.HMAC512(body.username + body.password))
            return@runOnBody ResponseFactory.create(HttpStatus.OK, Account.serializer(),
                                                    accounts.regenToken(account, Account(body.username,
                                                                                         account.email,
                                                                                         Utils.bcryptHash(body.password),
                                                                                         jwt, AccountType.SERVER_1000)))
        }
    }
}