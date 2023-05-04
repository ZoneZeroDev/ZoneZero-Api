package kiinse.me.zonezero.api.core.security

import com.auth0.jwt.JWT
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.exceptions.AccountException
import kiinse.me.zonezero.api.core.exceptions.AuthException
import kiinse.me.zonezero.api.core.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.core.security.enums.AccountType
import kiinse.me.zonezero.api.core.utils.Utils
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Account(val username: String,
                   val email: String,
                   val password: String,
                   val jwt: String,
                   val type: AccountType
) {

    companion object {

        fun byJwt(jwt: String?): Account {
            if (jwt.isNullOrBlank()) throw AuthException(HttpStatus.UNAUTHORIZED, "JWT token is empty!")
            val account = AccountQuery.getAccount(jwt) ?: throw AccountException(HttpStatus.UNAUTHORIZED, "Account with this JWT not found!")
            if (Date().after(account.expiresAtDate())) {
                throw AccountException(HttpStatus.UNAUTHORIZED, "JWT outdated!")
            }
            return account
        }

        fun valueOf(username: String): Account? {
            return AccountQuery.getAccountByName(username)
        }

        fun valueOf(email: String, password: String): Account? {
            return AccountQuery.getAccountByEmailPass(email, password)
        }
    }

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
        return "login=$username\nemail=$email\npassword=$password\njwt=$jwt\ntype=${type}\nexpiresAt=${expiresAtString()}"
    }

    fun checkPassword(password: String): Boolean {
        return Utils.bcryptCheck(password, this.password)
    }
}