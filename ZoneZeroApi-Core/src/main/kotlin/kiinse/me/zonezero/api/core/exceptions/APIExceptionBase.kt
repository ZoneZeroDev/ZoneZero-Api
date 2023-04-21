package kiinse.me.zonezero.api.core.exceptions

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

@Suppress("UNUSED")
open class APIExceptionBase : HttpStatusException {
    constructor(status: HttpStatus, message: String?) : super(status, message)
    constructor(status: HttpStatus, cause: Throwable?) : super(status, cause)
}