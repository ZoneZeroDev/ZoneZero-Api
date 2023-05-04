package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class UserRegister(val username: String,
                        val email: String,
                        val password: String)
