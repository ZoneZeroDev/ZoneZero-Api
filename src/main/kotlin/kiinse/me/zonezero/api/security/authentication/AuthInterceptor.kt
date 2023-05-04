package kiinse.me.zonezero.api.security.authentication

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.sentry.Sentry
import jakarta.inject.Singleton
import kiinse.me.zonezero.api.core.exceptions.AuthException
import kiinse.me.zonezero.api.core.security.Account
import kiinse.me.zonezero.api.security.annotations.Authentication
import kiinse.me.zonezero.api.core.security.enums.AccountType
import kiinse.me.zonezero.api.utils.RequestUtils
import kiinse.me.zonezero.api.utils.ResponseFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
@InterceptorBean(Authentication::class)
class AuthInterceptor : MethodInterceptor<Any, Any> {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private var request: HttpRequest<String?>? = null

    @Suppress("UNCHECKED_CAST")
    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? = runBlocking<Any?> {
        try {
            context?.parameterValueMap?.values?.forEach {
                if (it is HttpRequest<*>) {
                    val annotation = context.getAnnotation(Authentication::class.java)
                    if (annotation != null) {
                        request = it as HttpRequest<String?>
                        val permissions = annotation.values["permissions"] as Array<String>
                        if (permissions.contains(AccountType.ALL.toString())) {
                            return@runBlocking context.proceed()
                        }
                        // TODO: Проверка ядра
                        // TODO: проверка registeredQuery isValid(ip: String, userEmail: String, id: String
                        val bearer = RequestUtils.getBearer(request)
                        if (bearer != null && bearer == "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbFNlcnZpY2UiLCJleHAiOjU0NjExMzcxNzZ9.otIJdzE6GYTcZP1970_mxu4Ck0KRGS6HSqfD_rgogpQ") {
                           return@runBlocking context.proceed() // TODO: Убрать
                        }

                        val account = Account.byJwt(bearer)
                        val checkTimeout = async { AuthTimeout.checkTimeout(request!!) }
                        val checkPermissions = async { AuthChecks.checkPermissions(permissions, account.type) }
                        val checkAccountType = async { AuthChecks.checkAccountType(request!!, account) }
                        val checkIp = async { AuthChecks.checkIp(request!!, account) }
                        if (checkTimeout.await() && checkPermissions.await() && checkAccountType.await() && checkIp.await()) {
                            return@runBlocking context.proceed()
                        }
                        return@runBlocking ResponseFactory.create(it, AuthException(HttpStatus.UNAUTHORIZED, "UNEXPECTED ERROR"))
                    }
                }
            }
            return@runBlocking ResponseFactory.create(HttpStatus.UNAUTHORIZED)
        } catch (exception: Exception) {
            return@runBlocking when (exception) {
                is HttpStatusException -> {
                    if (exception.status == HttpStatus.INTERNAL_SERVER_ERROR) {
                        Sentry.captureException(exception)
                        logger.warn("Handled anomaly exception! Message: ${exception.message}")
                    }
                    return@runBlocking ResponseFactory.create(request, exception)
                }
                else                   -> {
                    logger.warn("Handled anomaly exception! Message: ${exception.message}")
                    Sentry.captureException(exception)
                    ResponseFactory.create(request, HttpStatus.INTERNAL_SERVER_ERROR, exception)
                }
            }
        }
    }
}