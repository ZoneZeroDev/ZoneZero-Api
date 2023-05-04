package kiinse.me.zonezero.api.utils

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import kiinse.me.zonezero.api.core.body.DataAnswer
import kiinse.me.zonezero.api.core.body.ExceptionAnswer
import kiinse.me.zonezero.api.core.body.MessageAnswer
import kiinse.me.zonezero.api.core.exceptions.RSAException
import kiinse.me.zonezero.api.security.ApiRSA
import kotlinx.serialization.json.Json

object ResponseFactory {

    fun create(request: HttpRequest<*>?, status: HttpStatus, exception: Exception): HttpResponse<DataAnswer> {
        return create(request, status, Json.encodeToString(ExceptionAnswer.serializer(), ExceptionAnswer(exception::class.toString(), exception.message ?: "")))
    }

    fun create(request: HttpRequest<*>?, status: HttpStatus, message: MessageAnswer): HttpResponse<DataAnswer> {
        return create(request, status, Json.encodeToString(MessageAnswer.serializer(), message))
    }

    fun create(request: HttpRequest<*>?, exception: HttpStatusException): HttpResponse<DataAnswer> {
        return create(request, exception.status, Json.encodeToString(ExceptionAnswer.serializer(), ExceptionAnswer(exception::class.toString(), exception.message ?: "")))
    }

    fun create(status: HttpStatus): HttpResponse<DataAnswer> {
        return HttpResponse.ok(DataAnswer()).status(status)
    }

    fun create(request: HttpRequest<*>?, status: HttpStatus, data: String?): HttpResponse<DataAnswer> {
        val publicKey = RequestUtils.getHeader(request, "publicKey")
        val rsa = ApiRSA.get()
        if (publicKey.isNullOrBlank()) {
            val response = HttpResponse.ok(DataAnswer(Json.encodeToString(MessageAnswer.serializer(), MessageAnswer("No public key in headers!"))))
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
            response.header("publicKey", rsa.getPublicKeyString())
            response.status(status)
            return response
        }
        return try {
            val encrypted = rsa.encrypt(data ?: "", rsa.recreatePublicKey(publicKey))
            val response = HttpResponse.ok(DataAnswer(encrypted.message))
            response.header("security", encrypted.aes)
            response.header("publicKey", rsa.getPublicKeyString())
            response.status(status)
            response
        } catch (e: RSAException) {
            create(request, e)
        }
    }
}