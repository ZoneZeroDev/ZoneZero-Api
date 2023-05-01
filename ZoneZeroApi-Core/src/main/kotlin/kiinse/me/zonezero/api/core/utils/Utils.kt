package kiinse.me.zonezero.api.core.utils

import org.mindrot.jbcrypt.BCrypt

object Utils {

    fun bcryptHash(input: String): String {
        return BCrypt.hashpw(input, BCrypt.gensalt())
    }

    fun bcryptCheck(input: String, hash: String): Boolean {
        return BCrypt.checkpw(input, hash)
    }
}
