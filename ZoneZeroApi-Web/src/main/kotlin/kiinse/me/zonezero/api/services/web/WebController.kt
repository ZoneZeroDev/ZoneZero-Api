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
import kiinse.me.zonezero.api.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.security.Account
import kiinse.me.zonezero.api.security.enums.AccountType
import kiinse.me.zonezero.api.services.ServiceUtils
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

@Controller("/web")
open class WebController {

    private val queryServers = QueryServerQuery
    private val registeredServers = RegisteredServerQuery

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Post("/getServerByCode")
    open fun getServerByCode(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->
            val server = queryServers.getServer(body.getString("code"))
                ?: return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND)
            return@runOnBody ResponseFactory.create(HttpStatus.OK, server.toJson())
        }
    }

    @Post("/registerUser")
    open fun registerUser(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->
            val username = body.getString("username")
            if (AccountQuery.hasLogin(username)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Username already exists!")
            val email = body.getString("email")
            if (AccountQuery.hasEmail(email)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Email already exists!")
            val password = body.getString("password")
            if (password.length < 8) {
                return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Password size cannot be less than 8")
            }
            val jwt = JWT.create()
                .withSubject(username)
                .withExpiresAt(Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(1000000000)))
                .sign(Algorithm.HMAC512(username + password))
            AccountQuery.createAccount(Account(username, body.getString("email"),
                                               ServiceUtils.bcryptHash(password), jwt, AccountType.SERVER_1000))
            return@runOnBody ResponseFactory.create(HttpStatus.OK, jwt)
        }
    }

    @Post("/authUser")
    open fun authUser(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->
            val password = body.getString("password")
            if (body.has("username")) {
                val username = body.getString("username")
                if (!AccountQuery.hasLogin(username)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, "User not found!")
                val account = AccountQuery.getAccountByNamePass(username, password) ?: return@runOnBody ResponseFactory.create(HttpStatus.FORBIDDEN, "Password mismatch!")
                return@runOnBody ResponseFactory.create(HttpStatus.OK, account.toJson())
            } else {
                val email = body.getString("email")
                if (!AccountQuery.hasEmail(email)) return@runOnBody ResponseFactory.create(HttpStatus.NOT_FOUND, "User not found!")
                val account = AccountQuery.getAccountByEmailPass(email, password) ?: return@runOnBody ResponseFactory.create(HttpStatus.FORBIDDEN, "Password mismatch!")
                return@runOnBody ResponseFactory.create(HttpStatus.OK, account.toJson())
            }
        }
    }

    @Post("/registerServer")
    open fun registerServer(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->

            return@runOnBody ResponseFactory.create(HttpStatus.OK)
        }
    }

    @Post("/getUserServers")
    open fun getUserServers(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->

            return@runOnBody ResponseFactory.create(HttpStatus.OK)
        }
    }

    @Post("/getServerInfo")
    open fun getServerInfo(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->

            return@runOnBody ResponseFactory.create(HttpStatus.OK)
        }
    }

    @Post("/regenToken")
    open fun regenToken(request: HttpRequest<String?>): HttpResponse<Response> {
        return RequestUtils.runOnBody(request) { body ->

            return@runOnBody ResponseFactory.create(HttpStatus.OK)
        }
    }
}