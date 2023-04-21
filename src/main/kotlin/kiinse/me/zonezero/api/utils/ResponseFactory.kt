package kiinse.me.zonezero.api.utils

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import kiinse.me.zonezero.api.core.exceptions.RSAException
import kiinse.me.zonezero.api.core.utils.Response
import kiinse.me.zonezero.api.security.ApiRSA
import org.json.JSONObject

object ResponseFactory {

    fun create(request: HttpRequest<*>?, status: HttpStatus, exception: Exception): HttpResponse<Response> {
        return create(request, status, stringToJson(exception.message))
    }

    fun create(request: HttpRequest<*>?, status: HttpStatus, message: String): HttpResponse<Response> {
        val data = JSONObject()
        data.put("message", message)
        return create(request, status, data)
    }

    fun create(request: HttpRequest<*>?, exception: HttpStatusException): HttpResponse<Response> {
        val data = JSONObject()
        data.put("message", exception.message)
        return create(request, exception.status, data)
    }

    fun create(status: HttpStatus): HttpResponse<Response> {
        return HttpResponse.ok(Response(null)).status(status)
    }

    fun create(request: HttpRequest<*>?, status: HttpStatus, data: JSONObject): HttpResponse<Response> {
        val publicKey = RequestUtils.getHeader(request, "publicKey")
        val rsa = ApiRSA.get()
        if (publicKey.isNullOrBlank()) {
            val response = HttpResponse.ok(Response(stringToJson("No public key in headers!").toMap()))
            response.header("publicKey", rsa.getPublicKeyString())
            response.status(status)
            return response
        }
        return try {
            val encrypted = rsa.encrypt(data, rsa.recreatePublicKey(publicKey))
            val response = HttpResponse.ok(Response(encrypted.message))
            response.header("security", encrypted.aes)
            response.header("publicKey", rsa.getPublicKeyString())
            response.status(status)
            response
        } catch (e: RSAException) {
            create(request, e)
        }
    }

    private fun stringToJson(string: String?): JSONObject {
        val json = JSONObject()
        json.put("message", string)
        return json
    }
}