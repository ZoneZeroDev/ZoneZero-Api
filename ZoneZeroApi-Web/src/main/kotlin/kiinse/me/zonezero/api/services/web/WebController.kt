package kiinse.me.zonezero.api.services.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.security.Account
import kiinse.me.zonezero.api.security.enums.AccountType
import kiinse.me.zonezero.api.services.ServiceUtils
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import kiinse.me.zonezero.api.services.body.*
import java.time.Instant
import java.util.concurrent.TimeUnit

@Controller("/web")
open class WebController {

    private val accounts = AccountQuery
    private val queryServers = QueryServerQuery
    private val registeredServers = RegisteredServerQuery

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Post("/getServerByCode")
    open fun getServerByCode(request: HttpRequest<String?>, @Body body: GetServerByCode): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            val server = queryServers.getServer(body.code) ?: return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "Server not found!")
            return@runWithCatch ResponseFactory.create(HttpStatus.OK, server.toJson())
        }
    }

    @Post("/registerUser")
    open fun registerUser(request: HttpRequest<String?>, @Body body: UserRegister): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            if (accounts.hasLogin(body.username)) return@runWithCatch ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Username already exists!")
            if (accounts.hasEmail(body.email)) return@runWithCatch ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Email already exists!")
            if (body.password.length < 8) {
                return@runWithCatch ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Password size cannot be less than 8")
            }
            val jwt = JWT.create()
                .withSubject(body.username)
                .withExpiresAt(Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(1000000000)))
                .sign(Algorithm.HMAC512(body.username + body.password))
            accounts.createAccount(Account(body.username, body.email,
                                               ServiceUtils.bcryptHash(body.password), jwt, AccountType.SERVER_1000))
            return@runWithCatch ResponseFactory.create(HttpStatus.OK, jwt)
        }
    }

    @Post("/authUser")
    open fun authUser(request: HttpRequest<String?>, @Body body: UserAuth): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            if (body.username != null) {
                if (!accounts.hasLogin(body.username)) return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "User not found!")
                val account = accounts.getAccountByNamePass(body.username, body.password) ?: return@runWithCatch ResponseFactory.create(HttpStatus.FORBIDDEN, "Password mismatch!")
                return@runWithCatch ResponseFactory.create(HttpStatus.OK, account.toJson())
            } else {
                if (!accounts.hasEmail(body.email!!)) return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "User not found!")
                val account = accounts.getAccountByEmailPass(body.email, body.password) ?: return@runWithCatch ResponseFactory.create(HttpStatus.FORBIDDEN, "Password mismatch!")
                return@runWithCatch ResponseFactory.create(HttpStatus.OK, account.toJson())
            }
        }
    }

    @Post("/registerServer")
    open fun registerServer(request: HttpRequest<String?>, @Body body: ServerRegister): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            val queryServer = queryServers.getServer(body.code) ?: return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "Server not found!")
            if (registeredServers.hasServer(queryServer)) {
                return@runWithCatch ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Server already registered!")
            }
            if (!accounts.hasEmail(body.email)) {
                return@runWithCatch ResponseFactory.create(HttpStatus.FORBIDDEN, "User not found!")
            }
            registeredServers.saveServer(queryServer, body.email)
            return@runWithCatch ResponseFactory.create(HttpStatus.OK)
        }
    }

    @Post("/getUserServers")
    open fun getUserServers(request: HttpRequest<String?>, @Body body: UserServers): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            if (body.email != null) {
                return@runWithCatch ResponseFactory.create(HttpStatus.OK, registeredServers.getServersByEmail(body.email))
            }
            val account = accounts.getAccountByName(body.username!!) ?: return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "User not found!")
            return@runWithCatch ResponseFactory.create(HttpStatus.OK, registeredServers.getServersByEmail(account.email))
        }
    }

    @Post("/getServerInfo")
    open fun getServerInfo(request: HttpRequest<String?>, @Body body: GetServerInfo): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            val server = registeredServers.getServer(body.serverId) ?: return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "Server not found!")
            return@runWithCatch ResponseFactory.create(HttpStatus.OK, server.toJson())
        }
    }

    @Post("/regenToken")
    open fun regenToken(request: HttpRequest<String?>, @Body body: RegenToken): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            if (!accounts.hasLogin(body.username)) return@runWithCatch ResponseFactory.create(HttpStatus.NOT_FOUND, "User not found!")
            val account = accounts.getAccountByNamePass(body.username, body.password) ?: return@runWithCatch ResponseFactory.create(HttpStatus.FORBIDDEN, "Password mismatch!")
            val jwt = JWT.create()
                .withSubject(body.username)
                .withExpiresAt(Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(1000000000)))
                .sign(Algorithm.HMAC512(body.username + body.password))
            return@runWithCatch ResponseFactory.create(HttpStatus.OK, accounts.regenToken(account, Account(body.username, account.email,
                                                                                                           ServiceUtils.bcryptHash(body.password), jwt, AccountType.SERVER_1000)).toJson())
        }
    }
}