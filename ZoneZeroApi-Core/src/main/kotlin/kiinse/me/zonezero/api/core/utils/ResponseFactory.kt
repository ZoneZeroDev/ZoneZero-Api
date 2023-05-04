package kiinse.me.zonezero.api.core.utils

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import kiinse.me.zonezero.api.core.body.ExceptionAnswer
import kiinse.me.zonezero.api.core.body.MessageAnswer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.*

object ResponseFactory {

    fun create(status: HttpStatus, exception: Exception): HttpResponse<String> {
        return create(status, ExceptionAnswer.serializer(), ExceptionAnswer(exception::class.toString(), exception.message ?: ""))
    }

    fun create(status: HttpStatus, message: String?): HttpResponse<String> {
        return create(status, MessageAnswer.serializer(), MessageAnswer(message ?: ""))
    }

    fun create(exception: HttpStatusException): HttpResponse<String> {
        return create(exception.status, ExceptionAnswer.serializer(), ExceptionAnswer(exception::class.toString(), exception.message ?: ""))
    }

    fun create(status: HttpStatus): HttpResponse<String> {
        return HttpResponse.ok("{}").status(status)
    }

    fun <T> create(status: HttpStatus, serializer: SerializationStrategy<T>, value: T): HttpResponse<String> {
        return HttpResponse.ok(Json.encodeToString(serializer, value)).status(status)
    }
}