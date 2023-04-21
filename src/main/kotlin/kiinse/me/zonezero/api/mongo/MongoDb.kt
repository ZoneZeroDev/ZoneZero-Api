package kiinse.me.zonezero.api.mongo

import kiinse.me.zonezero.api.core.config.ConfigFactory
import kiinse.me.zonezero.api.core.database.DataBaseSettings
import kiinse.me.zonezero.api.core.database.MongoConnection

object MongoDb : MongoConnection() {

    init {
        connect()
    }

    override fun getSettings(): DataBaseSettings {
        val config = ConfigFactory.config.getTableOrEmpty("mongo")
        return DataBaseSettings()
                .setHost(config["host"].toString())
                .setPort(config["port"].toString())
                .setLogin(config["login"].toString())
                .setPassword(config["password"].toString())
                .setDbName(config["dbName"].toString())
                .setAuthDb(config["authDb"].toString())
    }

    override fun createTables() {
        createCollectionIfNotExists("registeredServers")
        createCollectionIfNotExists("accounts")
    }
}