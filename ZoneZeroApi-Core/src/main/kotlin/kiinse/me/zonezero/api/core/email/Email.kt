package kiinse.me.zonezero.api.core.email

import kotlinx.serialization.Serializable

@Serializable
data class Email(val address: String, val subject: String, val template: EmailTemplate? = null)
