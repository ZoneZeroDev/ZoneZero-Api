package kiinse.me.zonezero.api.services.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import kiinse.me.zonezero.api.core.utils.*
import kiinse.me.zonezero.api.services.body.TestBody
import kotlinx.serialization.json.*

@Controller("/test")
open class TestController {

    private val helloWorld = "Hello World!"

    @Get("/status")
    fun status(request: HttpRequest<String?>): HttpResponse<String> {
        return ResponseFactory.create(HttpStatus.OK)
    }

    @Get("/getTestMessageGet")
    open fun getTestMessageGet(request: HttpRequest<String?>): HttpResponse<String> {
        return ResponseFactory.create(HttpStatus.OK, helloWorld)
    }

    @Post("/getTestMessagePost")
    open fun getTestMessagePost(request: HttpRequest<String?>): HttpResponse<String> {
        return RequestUtils.runOnBody(request, TestBody.serializer()) { body ->
            if (body.key != null && body.key.equals("i love you", ignoreCase = true)) {
                return@runOnBody ResponseFactory.create(HttpStatus.OK, helloWorld)
            }
            return@runOnBody ResponseFactory.create(HttpStatus.NOT_ACCEPTABLE)
        }
    }
}