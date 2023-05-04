package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class ServerRegister(val code: String,
                          val email: String)
