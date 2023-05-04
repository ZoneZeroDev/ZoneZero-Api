package kiinse.me.zonezero.api.core.twofa

import kiinse.me.zonezero.api.core.players.enums.TwoFaType
import kiinse.me.zonezero.api.core.twofa.enums.QueryType
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TwoFaData(
    val code: String,
    val queryType: QueryType,
    val twoFaType: TwoFaType,
    val address: String,
    val player: String,
    val data: String,
    @Serializable(with = InstantSerializer::class)
    val time: Instant
)