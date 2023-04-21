package kiinse.me.zonezero.api.core.utils

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import org.json.JSONObject

object ResponseFactory {

    fun create(status: HttpStatus, exception: Exception): HttpResponse<Response> {
        return create(status, stringToJson(exception.message))
    }

    fun create(status: HttpStatus, message: String): HttpResponse<Response> {
        val data = JSONObject()
        data.put("message", message)
        return create(status, data)
    }

    fun create(exception: HttpStatusException): HttpResponse<Response> {
        val data = JSONObject()
        data.put("message", exception.message)
        return create(exception.status, data)
    }

    fun create(status: HttpStatus): HttpResponse<Response> {
        return HttpResponse.ok(Response(null)).status(status)
    }

    fun create(status: HttpStatus, data: JSONObject): HttpResponse<Response> {
        return HttpResponse.ok(Response(data.toMap())).status(status)
    }

    private fun stringToJson(string: String?): JSONObject {
        val json = JSONObject()
        json.put("message", string)
        return json
    }
}