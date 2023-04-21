package kiinse.me.zonezero.api.email

data class EmailServiceSettings(val host: String,
                                val from: String,
                                val port: Int,
                                val username: String,
                                val password: String,
                                val isAuth: Boolean,
                                val isSSl: Boolean,
                                val sslTrust: String,
                                val isStartTTLs: Boolean,
                                val sslProtocol: String,
                                val isDebug: Boolean,
                                val mjmlId: String,
                                val mjmlSecret: String)