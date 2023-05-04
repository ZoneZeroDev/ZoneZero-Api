package kiinse.me.zonezero.api.core.email

import kotlinx.serialization.Serializable

@Serializable
data class EmailTemplate(val name: String, val data: Map<String, String>)