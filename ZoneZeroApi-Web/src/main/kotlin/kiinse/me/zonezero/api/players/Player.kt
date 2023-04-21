package kiinse.me.zonezero.api.players

import kiinse.me.zonezero.api.players.enums.TwoFaType
import kiinse.me.zonezero.api.security.Account

data class Player(val login: String?, var password: String?, var twoFaType: TwoFaType, var twoFa: String?, var lastIp: String?) {

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
}
