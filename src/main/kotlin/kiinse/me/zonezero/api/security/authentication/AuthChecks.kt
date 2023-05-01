package kiinse.me.zonezero.api.security.authentication

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.exceptions.AccountException
import kiinse.me.zonezero.api.core.exceptions.RequestException
import kiinse.me.zonezero.api.core.mongo.queries.RegisteredServerQuery
import kiinse.me.zonezero.api.core.security.Account
import kiinse.me.zonezero.api.core.security.enums.AccountType
import kiinse.me.zonezero.api.utils.RequestUtils

object AuthChecks {

    private val registeredServers: RegisteredServerQuery = RegisteredServerQuery

    @Throws(RequestException::class)
    fun checkIp(request: HttpRequest<String?>, account: Account): Boolean {
        if (account.type != AccountType.ADMIN && !registeredServers.hasServer(request.remoteAddress.address.hostAddress, account.email)) {
            throw RequestException(HttpStatus.FORBIDDEN, "This ip is not registered!")
        }
        return true
    }

    @Throws(RequestException::class)
    fun checkAccountType(request: HttpRequest<String?>, account: Account): Boolean {
        val accountType = account.type
        if (accountType != AccountType.ADMIN && accountType != AccountType.SERVER_1000) {
            val online = RequestUtils.getDecryptedHeader(request, "onpl")
            if (online.isNullOrEmpty()) return false
            val maxOnline = account.type.toString().split("_")[1].toInt()
            if (online.toInt() > maxOnline) {
                throw RequestException(HttpStatus.METHOD_NOT_ALLOWED, "Server online is more than $maxOnline players!")
            }
        }
        return true
    }

    @Throws(AccountException::class)
    fun checkPermissions(permissions: Array<String>, accountType: AccountType): Boolean {
        if (accountType == AccountType.ADMIN) return true
        permissions.forEach { if (accountType == AccountType.valueOf(it)) return true }
        throw AccountException(
            HttpStatus.UNAUTHORIZED,
            "You are not authorized for this request! You must have one of ${
                permissions.joinToString(
                    prefix = "[",
                    postfix = "]",
                    separator = ", "
                )
            } rights! Your rights is $accountType"
        )
    }
}