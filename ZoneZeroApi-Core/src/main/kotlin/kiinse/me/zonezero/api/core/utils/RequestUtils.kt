package kiinse.me.zonezero.api.core.utils

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import kiinse.me.zonezero.api.core.exceptions.RequestException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNUSED")
object RequestUtils {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun <T> runOnBody(request: HttpRequest<String?>, strategy: DeserializationStrategy<T>, runnable: (T) -> HttpResponse<String>): HttpResponse<String> {
        return runWithCatch { return@runWithCatch runnable(Json.decodeFromString(strategy, getBody(request))) }
    }

    fun runWithCatch(runnable: () -> HttpResponse<String>): HttpResponse<String> {
        return try {
            runnable()
        } catch (exception: Exception) {
            return when (exception) {
                is HttpStatusException -> {
                    if (exception.status == HttpStatus.INTERNAL_SERVER_ERROR) {
                        logger.warn("Handled anomaly exception! Message: ${exception.message}")
                    }
                    return ResponseFactory.create(exception)
                }
                else                   -> {
                    logger.warn("Handled anomaly exception! Message: ${exception.message}")
                    ResponseFactory.create(HttpStatus.INTERNAL_SERVER_ERROR, exception)
                }
            }
        }
    }

    fun getBearer(request: HttpRequest<*>?): String? {
        if (request == null) return null
        val headers = request.headers
        if (headers.isEmpty) return null
        val auth = headers["Authorization"]
        if (auth == null || !auth.contains("Bearer")) return null
        val token = auth.replace("Bearer ", "")
        return token.ifBlank { null }
    }

    fun getHeader(request: HttpRequest<*>?, header: String): String? {
        val headers = request!!.headers
        if (headers.isEmpty) return null
        return headers[header] ?: return null
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(RequestException::class)
    fun getBody(request: HttpRequest<*>?): String {
        if (request == null) return ""
        val optionalBody = (request as HttpRequest<String?>).body
        if (optionalBody.isEmpty || optionalBody.get().isBlank()) return ""
        return optionalBody.get().toByteArray().toString(charset("UTF-8"))
    }
}