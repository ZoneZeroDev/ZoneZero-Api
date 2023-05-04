package kiinse.me.zonezero.api.core.server

import kotlinx.serialization.Serializable

@Serializable
data class RegisteredServersList(val servers: HashMap<String, RegisteredServer>)