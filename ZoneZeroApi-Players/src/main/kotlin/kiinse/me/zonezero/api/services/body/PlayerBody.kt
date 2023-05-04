package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(val password: String? = null,
                      val newPassword: String? = null,
                      val oldPassword: String? = null,
                      val ip: String? = null,
                      val email: String? = null,
                      val code: String? = null)
