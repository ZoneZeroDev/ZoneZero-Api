package kiinse.me.zonezero.api.services.servers

import com.vdurmont.semver4j.Semver
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.server.QueryServer
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import kotlin.random.Random

object ServerUtils {

    private val serverQuery: QueryServerQuery = QueryServerQuery
    private val allowedCores: List<String> = listOf("Paper", "Spigot")
    private val allowedVersion: Semver = Semver("1.16.2")

    fun onServerAllow(request: HttpRequest<String?>, runnable: (QueryServer) -> HttpResponse<Response>): HttpResponse<Response> = runBlocking {
        val body = RequestUtils.getBody(request)
        val bukkitVersion = body.getString("bukkitVersion")
        val queryServer = async { getQueryServer(request, body) }
        val isAllowedCore = async { isAllowedCore(bukkitVersion) }
        val isAllowedVersion = async { isAllowedVersion(bukkitVersion) }
        return@runBlocking if (isAllowedCore.await() && isAllowedVersion.await()) {
            runnable(queryServer.await())
        } else {
            ResponseFactory.create(HttpStatus.FORBIDDEN, "Only allowed server cores $allowedCores with version ${allowedVersion.originalValue} and above")
        }
    }

    private fun isAllowedCore(version: String): Boolean {
        allowedCores.forEach {
            if (version.contains(it, ignoreCase = true)) return true
        }
        return false
    }

    private fun isAllowedVersion(version: String): Boolean {
        val ver = version.split("MC: ")[1].replace(")", "")
        return Semver(ver).isGreaterThanOrEqualTo(allowedVersion)
    }

    fun getQueryServer(request: HttpRequest<String?>, body: JSONObject): QueryServer {
        return QueryServer(
            body.getString("name"),
            body.getInt("maxPlayers"),
            getCode(),
            body.getString("pluginVersion"),
            body.getBoolean("allowEnd"),
            body.getBoolean("allowNether"),
            body.getBoolean("allowFlight"),
            body.getString("bukkitVersion"),
            body.getInt("monsterSpawnLimit"),
            body.getString("settingsIp"),
            body.getString("motd"),
            body.getInt("settingsPort"),
            body.getString("worldType"),
            body.getBoolean("generateStructures"),
            body.getInt("spawnRadius"),
            body.getInt("viewDistance"),
            body.getJSONArray("worlds").toSet(),
            request.remoteAddress.address.hostAddress
        )
    }

    private fun getCode(): String {
        val code = getRandomString(Random.nextInt(25, 30))
        if (serverQuery.hasServer(code)) {
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
}