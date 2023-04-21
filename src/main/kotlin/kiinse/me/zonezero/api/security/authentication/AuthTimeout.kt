package kiinse.me.zonezero.api.security.authentication

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.exceptions.TimeoutException
import kiinse.me.zonezero.api.utils.RequestUtils
import java.time.Instant

object AuthTimeout {

    private const val limitSeconds: Long = 20
    private const val limitRequests: Int = 5
    private val identifierCountMap = mutableMapOf<String, Int>()
    private val identifierTimeMap = mutableMapOf<String, Instant>()

    fun checkTimeout(request: HttpRequest<String?>): Boolean {
        if (request.path.contains("players", ignoreCase = true)) {
            val login = RequestUtils.getDecryptedHeader(request, "player") ?: return false
            val now = Instant.now()
            val lastTime = identifierTimeMap[login]
            if (lastTime != null && lastTime.plusSeconds(limitSeconds) > now) {
                identifierCountMap[login] = identifierCountMap.getOrDefault(login, 0) + 1
                if (identifierCountMap[login]!! > limitRequests) {
                    identifierTimeMap[login] = now
                    throw TimeoutException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "${HttpStatus.TOO_MANY_REQUESTS.reason}, please wait '${lastTime.plusSeconds(limitSeconds).epochSecond - now.epochSecond}' seconds"
                    )
                }
            } else {
                identifierTimeMap[login] = now
                identifierCountMap[login] = 1
            }
        }
        return true
    }
}