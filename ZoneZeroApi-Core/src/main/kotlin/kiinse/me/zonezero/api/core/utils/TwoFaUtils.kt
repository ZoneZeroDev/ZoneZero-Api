package kiinse.me.zonezero.api.core.utils

import io.micronaut.http.HttpRequest
import kiinse.me.zonezero.api.core.config.Addresses
import kiinse.me.zonezero.api.core.services.ServerAnswer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import java.nio.charset.StandardCharsets

object TwoFaUtils {

    private const val timeout = 20000

    fun <T> post(request: HttpRequest<String?>, address: Addresses, path: String, strategy: SerializationStrategy<T>, value: T): ServerAnswer {
        return try {
            return getServerAnswer(getRequestPost(address.value + path, request, strategy, value).execute().returnResponse())
        } catch (e: Exception) {
            ServerAnswer(500)
        }
    }

    private fun <T> getRequestPost(address: String, req: HttpRequest<String?>, strategy: SerializationStrategy<T>, value: T): Request {
        val request = Request.Post(address)
        request.connectTimeout(timeout)
        request.socketTimeout(timeout)
        request.useExpectContinue()
        request.version(HttpVersion.HTTP_1_1)
        request.bodyString(Json.encodeToString(strategy, value), ContentType.APPLICATION_JSON)
        req.headers.forEachValue { key, content ->
            try {
                if (key != "Content-Length")  {
                    request.addHeader(key, content)
                }
            } catch (e: Exception) {e.printStackTrace()}
        }
        return request
    }

    private fun getServerAnswer(response: HttpResponse): ServerAnswer {
        val content = if (response.entity != null) {
            try {
                IOUtils.toString(response.entity.content, StandardCharsets.UTF_8)
            } catch (e: Exception) { "" }
        } else { "" }
        val responseCode = response.statusLine.statusCode
        return ServerAnswer(responseCode, content)
    }
}
