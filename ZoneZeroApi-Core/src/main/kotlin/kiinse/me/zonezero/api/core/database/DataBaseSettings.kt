package kiinse.me.zonezero.api.core.database

class DataBaseSettings {
    var host = "localhost"
        private set
    var port = "27017"
        private set
    var login = ""
        private set
    var password = ""
        private set
    var dbName = "kiinseapi"
        private set
    var authDb = "admin"
        private set

    @Throws(IllegalArgumentException::class)
    fun setHost(sqlHost: String): DataBaseSettings {
        require(sqlHost.isNotBlank()) { "Host is empty" }
        host = sqlHost
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setPort(sqlPort: String): DataBaseSettings {
        port = sqlPort
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setLogin(sqlLogin: String): DataBaseSettings {
        require(sqlLogin.isNotBlank()) { "Login is empty" }
        login = sqlLogin
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setPassword(sqlPassword: String): DataBaseSettings {
        require(sqlPassword.isNotBlank()) { "Password is empty" }
        password = sqlPassword
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setDbName(sqldbName: String): DataBaseSettings {
        require(sqldbName.isNotBlank()) { "Database name is empty" }
        dbName = sqldbName
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setAuthDb(authDb: String): DataBaseSettings {
        require(authDb.isNotBlank()) { "Database name is empty" }
        this.authDb = authDb
        return this
    }
}