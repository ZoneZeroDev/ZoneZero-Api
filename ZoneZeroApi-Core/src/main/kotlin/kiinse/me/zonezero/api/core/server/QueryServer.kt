package kiinse.me.zonezero.api.core.server

import kiinse.me.zonezero.api.core.mongo.queries.QueryServerQuery
import kotlinx.serialization.Serializable

@Serializable
data class QueryServer(
    val name: String,
    val maxPlayers: Int,
    val code: String,
    val pluginVersion: String,
    val allowEnd: Boolean,
    val allowNether: Boolean,
    val allowFlight: Boolean,
    val bukkitVersion: String,
    val monsterSpawnLimit: Int,
    val settingsIp: String,
    val motd: String,
    val settingsPort: Int,
    val worldType: String,
    val generateStructures: Boolean,
    val spawnRadius: Int,
    val viewDistance: Int,
    val worlds: Set<String>,
    val ip: String) {

    companion object {
        fun valueOf(code: String): QueryServer? {
            return QueryServerQuery.getServer(code)
        }
    }

    fun getId(): String {
        return name + ip + settingsPort + maxPlayers + bukkitVersion
    }
}