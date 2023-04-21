package kiinse.me.zonezero.api.services.players

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.exceptions.PlayerException
import kiinse.me.zonezero.api.core.exceptions.QueryException
import kiinse.me.zonezero.api.mongo.queries.PlayerQuery
import kiinse.me.zonezero.api.mongo.queries.TwoFaQuery
import kiinse.me.zonezero.api.players.Player
import kiinse.me.zonezero.api.players.enums.TwoFaType
import kiinse.me.zonezero.api.twofa.enums.QueryType
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import kiinse.me.zonezero.api.services.ServiceUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.Instant

@Controller("/players")
open class PlayerController {

    private val playerNotFound = "Player not found!"
    private val playerQuery: PlayerQuery = PlayerQuery
    private val tfaQuery: TwoFaQuery = TwoFaQuery

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<Response> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Post("/login/standard")
    open fun loginPlayerStandard(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayerPass(request, "password") { body, player ->
            runBlocking {
                if (player == null) throw PlayerException(HttpStatus.NOT_FOUND, playerNotFound)
                val ip = async { PlayerUtils.getPlayerIp(body) }
                if (player.twoFaType != TwoFaType.NONE) {
                    tfaQuery.createQuery(request, PlayerUtils.getTwoFaData(QueryType.AUTH, player, body))
                    return@runBlocking ResponseFactory.create(HttpStatus.ACCEPTED)
                }
                playerQuery.updatePlayer(player.setIp(ip.await()))
                return@runBlocking ResponseFactory.create(HttpStatus.OK)
            }
        }
    }

    @Post("/login/ip")
    open fun loginPlayerIp(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayer(request) { body, player ->
            runBlocking {
                if (player == null) throw PlayerException(HttpStatus.NOT_FOUND, playerNotFound)
                val ip = PlayerUtils.getPlayerIp(body)
                if (!ip.isNullOrEmpty() && ip != "null" && ip != "127.0.0.1" && player.lastIp == ip) {
                    if (player.twoFaType != TwoFaType.NONE) {
                        tfaQuery.createQuery(request, PlayerUtils.getTwoFaData(QueryType.AUTH, player, body))
                        return@runBlocking ResponseFactory.create(HttpStatus.ACCEPTED)
                    }
                    return@runBlocking ResponseFactory.create(HttpStatus.OK)
                }
                return@runBlocking ResponseFactory.create(HttpStatus.FORBIDDEN)
            }
        }
    }

    @Post("/register")
    open fun registerPlayer(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayer(request) { body, player ->
            runBlocking {
                if (player != null) throw PlayerException(HttpStatus.FORBIDDEN, "Player already registered!")
                val login = async {
                    val login = RequestUtils.getHeader(request, "player")
                    if (login.isNullOrBlank()) throw PlayerException(HttpStatus.UNAUTHORIZED, "Player login is empty!")
                    return@async login
                }
                val password = async {
                    val password = PlayerUtils.getStringOrNull(body, "password")
                    if (password.isNullOrBlank()) throw PlayerException(HttpStatus.UNAUTHORIZED, "Player password is empty!")
                    if (password.length < 8) throw PlayerException(HttpStatus.NOT_ACCEPTABLE, "Password size cannot be less than 8")
                    return@async password
                }
                val ip = async { PlayerUtils.getStringOrNull(body, "ip") ?: "" }
                playerQuery.createPlayer(Player(login.await(), ServiceUtils.bcryptHash(password.await()), TwoFaType.NONE, "", ip.await()))
                return@runBlocking ResponseFactory.create(HttpStatus.OK)
            }
        }
    }

    @Post("/changePassword")
    open fun changePassword(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayerPass(request, "oldPassword") { body, player ->
            runBlocking {
                if (player == null) throw PlayerException(HttpStatus.NOT_FOUND, playerNotFound)
                val password = body.getString("newPassword")
                if (password.length < 8) {
                    return@runBlocking ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, "Password size cannot be less than 8")
                }
                if (player.twoFaType != TwoFaType.NONE) {
                    tfaQuery.createQuery(request, PlayerUtils.getTwoFaData(QueryType.CHANGE_PASSWORD, player, body))
                    return@runBlocking ResponseFactory.create(HttpStatus.ACCEPTED)
                }
                playerQuery.updatePlayer(player.setPassword(password).setIp("password_changed"))
                return@runBlocking ResponseFactory.create(HttpStatus.OK)
            }
        }
    }

    @Post("/2fa/enable")
    open fun twoFaEnable(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayerPass(request, "password") { body, player ->
            runBlocking {
                val email = body.getString("email")
                if (player == null) throw PlayerException(HttpStatus.NOT_FOUND, playerNotFound)
                if (!PlayerUtils.checkEmail(email)) {
                    return@runBlocking ResponseFactory.create(HttpStatus.METHOD_NOT_ALLOWED)
                }
                if (player.twoFaType != TwoFaType.NONE) {
                    return@runBlocking ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE, player.twoFaType.toString())
                }
                tfaQuery.createQuery(request, PlayerUtils.getTwoFaData(QueryType.ENABLE_TFA, player.setTwoFaType(TwoFaType.EMAIL).setTwoFa(email), body))
                return@runBlocking ResponseFactory.create(HttpStatus.OK)
            }
        }
    }

    @Post("/2fa/disable")
    open fun twoFaDisable(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayerPass(request, "password") { body, player ->
            runBlocking {
                if (player == null) throw PlayerException(HttpStatus.FORBIDDEN, playerNotFound)
                if (player.twoFaType == TwoFaType.NONE) {
                    return@runBlocking ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE)
                }
                tfaQuery.createQuery(request, PlayerUtils.getTwoFaData(QueryType.DISABLE_TFA, player, body))
                return@runBlocking ResponseFactory.create(HttpStatus.OK)
            }
        }
    }

    @Post("/2fa/code")
    open fun twoFaCode(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayer(request) { body, player ->
            runBlocking {
                if (player == null) throw PlayerException(HttpStatus.NOT_FOUND, playerNotFound)
                val query = tfaQuery.getQuery(body.getString("code"), player) ?: return@runBlocking ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE)
                if (Instant.now() > query.time) throw QueryException(HttpStatus.FORBIDDEN, "2FA code is outdated!")
                val queryBody = JSONObject(query.data)
                when (query.queryType) {
                    QueryType.ENABLE_TFA      -> {
                        playerQuery.updatePlayer(player.setTwoFa(queryBody.getString("email")).setTwoFaType(TwoFaType.EMAIL))
                    }

                    QueryType.DISABLE_TFA     -> {
                        playerQuery.updatePlayer(player.setTwoFa("").setTwoFaType(TwoFaType.NONE))
                    }

                    QueryType.AUTH            -> {
                        return@runBlocking ResponseFactory.create(HttpStatus.OK, query.queryType.toString())
                    }

                    QueryType.CHANGE_PASSWORD -> {
                        playerQuery.updatePlayer(player.setPassword(queryBody.getString("newPassword")).setIp("password_changed"))
                    }
                }
                return@runBlocking ResponseFactory.create(HttpStatus.OK, query.queryType.toString())
            }
        }
    }

    @Post("/check")
    open fun checkPlayer(request: HttpRequest<String?>): HttpResponse<Response> {
        return PlayerUtils.runOnPlayer(request) { _, player ->
            runBlocking {
                if (player == null) throw PlayerException(HttpStatus.NOT_FOUND, playerNotFound)
                return@runBlocking ResponseFactory.create(HttpStatus.OK)
            }
        }
    }
}