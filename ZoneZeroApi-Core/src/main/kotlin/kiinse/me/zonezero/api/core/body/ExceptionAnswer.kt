package kiinse.me.zonezero.api.core.body

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionAnswer(val exceptionClass: String = "", val message: String = "")
