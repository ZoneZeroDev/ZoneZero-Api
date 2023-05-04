package kiinse.me.zonezero.api.services.servers

import com.vdurmont.semver4j.Semver
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.core.server.QueryServer
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.ResponseFactory
import kiinse.me.zonezero.api.services.body.ServerInfoBody
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

object ServerUtils {

    private val serverQuery: QueryServerQuery = QueryServerQuery
    private val allowedCores: List<String> = listOf("Paper", "Spigot")
    private val allowedVersion: Semver = Semver("1.16.2")

    fun onServerAllow(request: HttpRequest<String?>, runnable: (QueryServer) -> HttpResponse<String>): HttpResponse<String>{
        return RequestUtils.runOnBody(request, ServerInfoBody.serializer()) { body ->
            return@runOnBody runBlocking {
                val bukkitVersion = body.bukkitVersion
                val queryServer = async {
                    QueryServer(body.name,
                                body.maxPlayers,
                                getCode(),
                                body.pluginVersion,
                                body.allowEnd,
                                body.allowNether,
                                body.allowFlight,
                                body.bukkitVersion,
                                body.monsterSpawnLimit,
                                body.settingsIp,
                                body.motd,
                                body.settingsPort,
                                body.worldType,
                                body.generateStructures,
                                body.spawnRadius,
                                body.viewDistance,
                                body.worlds,
                                request.remoteAddress.address.hostAddress)
                }
                val isAllowedCore = async { isAllowedCore(bukkitVersion) }
                val isAllowedVersion = async { isAllowedVersion(bukkitVersion) }
                if (isAllowedCore.await() && isAllowedVersion.await()) {
                    return@runBlocking runnable(queryServer.await())
                } else {
                    return@runBlocking ResponseFactory.create(HttpStatus.FORBIDDEN, "Only allowed server cores $allowedCores with version ${allowedVersion.originalValue} and above")
                }
            }
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