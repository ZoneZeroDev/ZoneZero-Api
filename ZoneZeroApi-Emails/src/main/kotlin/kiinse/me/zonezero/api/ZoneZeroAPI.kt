package kiinse.me.zonezero.api

import io.micronaut.runtime.Micronaut.run
import io.sentry.Sentry
import kiinse.me.zonezero.api.core.config.ConfigFactory
import kiinse.me.zonezero.api.email.EmailService
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.ResponseFactory

fun main(args: Array<String>) {
    setupSentry()
    loadAll()
    run(EmailService::class.java, *args)
}

fun loadAll() {
    ConfigFactory
    RequestUtils
    ResponseFactory
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