package kiinse.me.zonezero.api.security.authentication

import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.exceptions.AccountException
import kiinse.me.zonezero.api.core.exceptions.AuthException
import kiinse.me.zonezero.api.mongo.queries.AccountQuery
import kiinse.me.zonezero.api.security.Account
import java.util.*

object AuthService {

    private val accountQuery: AccountQuery = AccountQuery

    @Throws(AccountException::class, AuthException::class)
    fun login(jwt: String?): Account {
        if (jwt.isNullOrBlank()) throw AuthException(HttpStatus.UNAUTHORIZED, "JWT token is empty!")
        val account = accountQuery.getAccount(jwt) ?: throw AccountException(HttpStatus.UNAUTHORIZED, "Account with this JWT not found!")
        if (Date().after(account.expiresAtDate())) {
            throw AccountException(HttpStatus.UNAUTHORIZED, "JWT outdated!")
        }
        return account
    }
}