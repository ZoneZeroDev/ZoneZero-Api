package kiinse.me.zonezero.api.core.server

import kiinse.me.zonezero.api.core.mongo.queries.RegisteredServerQuery
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredServer(
    val email: String,
    val name: String,
    val maxPlayers: Int,
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
        fun valueOf(id: String): RegisteredServer? {
            return RegisteredServerQuery.getServer(id)
        }

        fun valueOf(queryServer: QueryServer): RegisteredServer? {
            return RegisteredServerQuery.getServer(queryServer)
        }
    }

    fun getId(): String {
        return name + ip + settingsPort + maxPlayers + bukkitVersion + email
    }
}