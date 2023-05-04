package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class UserServers(val email: String? = null,
                       val username: String?)
