package kiinse.me.zonezero.api.services.body

import kotlinx.serialization.Serializable

@Serializable
data class GetServerByCode(val code: String)
