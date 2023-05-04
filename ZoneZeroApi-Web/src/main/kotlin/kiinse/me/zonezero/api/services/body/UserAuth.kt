package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class UserAuth(val username: String? = null,
                    val email: String? = null,
                    val password: String)
