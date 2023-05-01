package kiinse.me.zonezero.api

import io.micronaut.runtime.Micronaut.run
import io.sentry.Sentry
import kiinse.me.zonezero.api.core.config.ConfigFactory
import kiinse.me.zonezero.api.core.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.core.mongo.queries.PlayerQuery
import kiinse.me.zonezero.api.core.mongo.queries.QueryServerQuery
import kiinse.me.zonezero.api.core.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.services.web.WebController
import kiinse.me.zonezero.api.core.utils.RequestUtils
import kiinse.me.zonezero.api.core.utils.ResponseFactory

fun main(args: Array<String>) {
    setupSentry()
    loadAll()
    run(WebController::class.java, *args)
}

private fun loadAll() {
    ConfigFactory
    AccountQuery
    RegisteredServerQuery
    PlayerQuery
    QueryServerQuery
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