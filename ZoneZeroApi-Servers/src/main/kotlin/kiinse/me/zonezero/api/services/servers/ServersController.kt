package kiinse.me.zonezero.api.services.servers

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.core.utils.ResponseFactory

@Controller("/server")
open class ServersController {

    private val serverQuery: QueryServerQuery = QueryServerQuery
    private val accountQuery: AccountQuery = AccountQuery
    private val registeredServer: RegisteredServerQuery = RegisteredServerQuery

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Post("/getCode")
    open fun getCode(request: HttpRequest<String?>): HttpResponse<Response> {
        return ServerUtils.onServerAllow(request) { queryServer ->
            val token = RequestUtils.getBearer(request)
            if (token != null) {
                if (token == "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbFNlcnZpY2UiLCJleHAiOjU0NjExMzcxNzZ9.otIJdzE6GYTcZP1970_mxu4Ck0KRGS6HSqfD_rgogpQ") {
                    return@onServerAllow ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE) // TODO: Убрать
                }
                val account = accountQuery.getAccount(token)
                if (account != null && registeredServer.isValid(queryServer.ip, account.email, queryServer.getId())) {
                    return@onServerAllow ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE)
                }
            }
            serverQuery.saveServer(queryServer)
            ResponseFactory.create(HttpStatus.OK, queryServer.code)
        }
    }
}