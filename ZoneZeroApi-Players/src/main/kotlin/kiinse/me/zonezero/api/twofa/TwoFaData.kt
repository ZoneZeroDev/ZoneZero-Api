package kiinse.me.zonezero.api.twofa

import kiinse.me.zonezero.api.players.enums.TwoFaType
import kiinse.me.zonezero.api.twofa.enums.QueryType
import java.time.Instant

data class TwoFaData(
    val code: String,
    val queryType: QueryType,
    val twoFaType: TwoFaType,
    val address: String,
    val player: String,
    val data: String,
    val time: Instant
)