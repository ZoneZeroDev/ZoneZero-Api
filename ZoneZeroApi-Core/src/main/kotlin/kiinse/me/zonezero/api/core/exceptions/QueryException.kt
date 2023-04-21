package kiinse.me.zonezero.api.core.exceptions

import io.micronaut.http.HttpStatus

@Suppress("UNUSED")
class QueryException : APIExceptionBase {
    constructor(status: HttpStatus, message: String?) : super(status, message)
    constructor(status: HttpStatus, cause: Throwable?) : super(status, cause)
}