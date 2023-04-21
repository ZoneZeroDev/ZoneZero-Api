package kiinse.me.zonezero.api.utils

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.sentry.Sentry
import kiinse.me.zonezero.api.core.exceptions.RSAException
import kiinse.me.zonezero.api.core.exceptions.RequestException
import kiinse.me.zonezero.api.core.rsa.data.EncryptedMessage
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.security.Account
import kiinse.me.zonezero.api.security.ApiRSA
import kiinse.me.zonezero.api.security.authentication.AuthService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNUSED")
object RequestUtils {

    private val authService: AuthService = AuthService
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun runOnAccount(request: HttpRequest<String?>, runnable: (Account) -> HttpResponse<Response>): HttpResponse<Response> {
        return runWithCatch(request) { return@runWithCatch runnable(authService.login(getBearer(request))) }
    }

    fun runOnBody(request: HttpRequest<String?>, runnable: (JSONObject, Account) -> HttpResponse<Response>): HttpResponse<Response> = runBlocking {
        val body = async { getBody(request) }
        val bearer = async { getBearer(request) }
        val account = async { authService.login(bearer.await()) }
        return@runBlocking runWithCatch(request) {
            return@runWithCatch runBlocking {
                runnable(body.await(), account.await())
            }
        }
    }

    fun runOnBody(request: HttpRequest<String?>, runnable: (JSONObject) -> HttpResponse<Response>): HttpResponse<Response> {
        return runWithCatch(request) { return@runWithCatch runnable(getBody(request)) }
    }

    private fun runWithCatch(request: HttpRequest<String?>, runnable: () -> HttpResponse<Response>): HttpResponse<Response> {
        return try {
            runnable()
        } catch (exception: Exception) {
            return when (exception) {
                is HttpStatusException -> {
                    if (exception.status == HttpStatus.INTERNAL_SERVER_ERROR) {
                        Sentry.captureException(exception)
                        logger.warn("Handled anomaly exception! Message:" + exception.message)
                    }
                    return ResponseFactory.create(request, exception)
                }
                else                   -> {
                    logger.warn("Handled anomaly exception! Message:" + exception.message)
                    Sentry.captureException(exception)
                    ResponseFactory.create(request, HttpStatus.INTERNAL_SERVER_ERROR, exception)
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

    fun getDecryptedHeader(request: HttpRequest<*>?, header: String): String? {
        val headers = request!!.headers
        if (headers.isEmpty) return null
        return ApiRSA.get().decrypt(headers[header] ?: return null)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(JSONException::class, RSAException::class, RequestException::class)
    fun getBody(request: HttpRequest<*>?): JSONObject {
        if (request == null) return JSONObject()
        val optionalBody = (request as HttpRequest<String?>).body
        if (optionalBody.isEmpty || optionalBody.get().isBlank()) return JSONObject()
        val aes = getHeader(request, "security")
        if (aes.isNullOrEmpty()) throw RequestException(HttpStatus.NOT_ACCEPTABLE, "No security key in headers!")
        return ApiRSA.get().decrypt(EncryptedMessage(aes, optionalBody.get().toByteArray().toString(charset("UTF-8"))))
    }
}