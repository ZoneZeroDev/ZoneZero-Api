package kiinse.me.zonezero.api.core.players

import kiinse.me.zonezero.api.core.mongo.queries.PlayerQuery
import kiinse.me.zonezero.api.core.players.enums.TwoFaType
import kiinse.me.zonezero.api.core.security.Account
import kiinse.me.zonezero.api.core.utils.Utils

data class Player(val login: String?, var password: String?, var twoFaType: TwoFaType, var twoFa: String?, var lastIp: String?) {

    companion object {
        fun valueOf(login: String?): Player? {
            return PlayerQuery.getPlayer(login)
        }

        fun valueOf(login: String?, password: String?): Player? {
            return PlayerQuery.getPlayer(login, password)
        }
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
        return "login=$login\npassword=$password\nemail=$twoFa"
    }

    fun setPassword(password: String?): Player {
        this.password = password
        return this
    }

    fun setIp(ip: String?): Player {
        this.lastIp = ip
        return this
    }

    fun setTwoFa(twoFa: String?): Player {
        this.twoFa = twoFa
        return this
    }

    fun setTwoFaType(twoFaType: TwoFaType): Player {
        this.twoFaType = twoFaType
        return this
    }

    fun checkPassword(password: String): Boolean {
        return Utils.bcryptCheck(password, this.password!!)
    }
}
