package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class RegenToken(val username: String,
                      val password: String)
