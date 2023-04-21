package kiinse.me.zonezero.api.security

import com.auth0.jwt.JWT
import kiinse.me.zonezero.api.security.enums.AccountType
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Account(val username: String,
                   val email: String,
                   val jwt: String,
                   val type: AccountType
) {

    fun expiresAtDate(): Date {
        return JWT.decode(jwt).expiresAt
    }

    fun expiresAtString(): String {
        return SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss z").format(expiresAtDate())
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Account) {
            return other.hashCode() == hashCode()
        }
        return false
    }

    override fun toString(): String {
        return "login=$username\nemail=$email\njwt=$jwt\ntype=${type}\nexpiresAt=${expiresAtString()}"
    }
}