package kiinse.me.zonezero.api

import io.micronaut.runtime.Micronaut.run
import io.sentry.Sentry
import kiinse.me.zonezero.api.core.config.ConfigFactory
import kiinse.me.zonezero.api.core.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.core.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.security.ApiRSA
import kiinse.me.zonezero.api.security.authentication.AuthChecks
import kiinse.me.zonezero.api.security.authentication.AuthInterceptor
import kiinse.me.zonezero.api.security.authentication.AuthTimeout
import kiinse.me.zonezero.api.services.ServiceUtils
import kiinse.me.zonezero.api.utils.RequestUtils
import kiinse.me.zonezero.api.utils.ResponseFactory

fun main(args: Array<String>) {
    setupSentry()
    loadAll()
    run(AuthInterceptor::class.java, *args)
}

private fun loadAll() {
    ConfigFactory
    AccountQuery
    RegisteredServerQuery
    AuthChecks
    ServiceUtils
    RequestUtils
    ResponseFactory
    AuthTimeout
    ApiRSA
}

private fun setupSentry() {
    val dsn = ConfigFactory.config.getTableOrEmpty("sentry").getString("dsn") { "" }
    if (dsn.isNotEmpty()) {
        Sentry.init { options ->
            options.dsn = dsn
            options.tracesSampleRate = 1.0
            options.isDebug = false
        }
    }
}