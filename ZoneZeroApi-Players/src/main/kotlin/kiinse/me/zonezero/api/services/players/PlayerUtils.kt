package kiinse.me.zonezero.api.services.players

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.exceptions.PlayerException
import kiinse.me.zonezero.api.core.mongo.queries.PlayerQuery
import kiinse.me.zonezero.api.core.mongo.queries.TwoFaQuery
import kiinse.me.zonezero.api.core.players.Player
import kiinse.me.zonezero.api.core.twofa.TwoFaData
import kiinse.me.zonezero.api.core.twofa.enums.QueryType
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.Response
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.random.Random

object PlayerUtils {

    private val playerQuery: PlayerQuery = PlayerQuery
    private val tfaQuery: TwoFaQuery = TwoFaQuery
    private val emailPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+"
    )

    fun checkEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    private fun getCode(): String {
        val code = getRandomString(Random.nextInt(10, 15))
        if (tfaQuery.hasQuery(code)) {
            return getCode()
        }
        return code
    }

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun runOnPlayer(request: HttpRequest<String?>, runnable: (JSONObject, Player?) -> HttpResponse<Response>): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            return@runWithCatch runBlocking {
                val body = async { RequestUtils.getBody(request) }
                val playerName = async { RequestUtils.getHeader(request, "player") }
                val player = async { Player.valueOf(playerName.await()) }
                runnable(body.await(), player.await())
            }
        }
    }

    fun runOnPlayerPass(request: HttpRequest<String?>, passwordKey: String, runnable: (JSONObject, Player?) -> HttpResponse<Response>): HttpResponse<Response> {
        return RequestUtils.runWithCatch {
            return@runWithCatch runBlocking {
                val body = async { RequestUtils.getBody(request) }
                val playerName = async { RequestUtils.getHeader(request, "player") }
                val password = async { body.await().getString(passwordKey) }
                val player = async { Player.valueOf(playerName.await(), password.await()) }
                if (player.await() == null && playerQuery.hasPlayer(playerName.await())) {
                    throw PlayerException(HttpStatus.UNAUTHORIZED, "Wrong password!")
                }
                runnable(body.await(), player.await())
            }
        }
    }

    suspend fun getTwoFaData(type: QueryType, player: Player, body: JSONObject): TwoFaData = runBlocking {
        val code = async { getCode() }
        val stringBody = async { body.toString() }
        return@runBlocking TwoFaData(
            code.await(),
            type,
            player.twoFaType,
            player.twoFa!!,
            player.login!!,
            stringBody.await(),
            Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(10L))
        )
    }

    fun getStringOrNull(body: JSONObject, key: String): String? {
        return try {
            body.getString(key)
        } catch (e: Exception) {
            null
        }
    }

    fun getPlayerIp(body: JSONObject): String? {
        return getStringOrNull(body, "ip")?.replace("/", "")
    }
}