package kiinse.me.zonezero.api.core.body

import kotlinx.serialization.Serializable

@Serializable
data class MessageAnswer(val message: String? = "")
