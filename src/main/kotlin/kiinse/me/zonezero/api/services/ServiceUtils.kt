package kiinse.me.zonezero.api.services

import io.micronaut.http.HttpRequest
import io.sentry.Sentry
import kiinse.me.zonezero.api.core.config.Addresses
import kiinse.me.zonezero.api.core.services.ServerAnswer
import kiinse.me.zonezero.api.security.ApiRSA
import kiinse.me.zonezero.api.utils.RequestUtils
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object ServiceUtils {

    private const val timeout = 20000

    fun get(request: HttpRequest<String?>, address: Addresses, path: String): ServerAnswer {
        return try {
            getServerAnswer(getRequestGet(address.value + path, request).execute().returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ServerAnswer(500, JSONObject())
        }
    }

    fun post(request: HttpRequest<String?>, address: Addresses, path: String): ServerAnswer {
        return try {
            getServerAnswer(getRequestPost(address.value + path, request).execute().returnResponse())
        } catch (e: Exception) {
            Sentry.captureException(e)
            ServerAnswer(500, JSONObject())
        }
    }

    private fun getRequestGet(address: String, req: HttpRequest<String?>): Request {
        val request = Request.Get(address)
        request.connectTimeout(timeout)
        request.socketTimeout(timeout)
        req.headers.forEachValue { key, value ->
            try {
                if (key != "Content-Length")  {
                    if (key == "player") {
                        request.addHeader("player", ApiRSA.get().decrypt(value))
                    }
                    request.addHeader(key, value)
                }
            } catch (e: Exception) {e.printStackTrace()}
        }
        return request
    }

    private fun getRequestPost(address: String, req: HttpRequest<String?>): Request {
        val request = Request.Post(address)
        request.connectTimeout(timeout)
        request.socketTimeout(timeout)
        request.useExpectContinue()
        request.version(HttpVersion.HTTP_1_1)
        request.bodyString(RequestUtils.getBody(req).toString(), ContentType.APPLICATION_JSON)
        req.headers.forEachValue { key, value ->
            try {
                if (key != "Content-Length")  {
                    if (key == "player") {
                        request.addHeader("player", ApiRSA.get().decrypt(value))
                    }
                    request.addHeader(key, value)
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
        return ServerAnswer(responseCode, JSONObject(content))
    }
}
