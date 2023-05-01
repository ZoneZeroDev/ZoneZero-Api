package kiinse.me.zonezero.api.core.server

import kiinse.me.zonezero.api.core.mongo.queries.RegisteredServerQuery
import org.json.JSONObject

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
    val worlds: Set<Any>,
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

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("_id", getId())
        json.put("email", email)
        json.put("name", name)
        json.put("maxPlayers", maxPlayers)
        json.put("pluginVersion", pluginVersion)
        json.put("allowEnd", allowEnd)
        json.put("allowNether", allowNether)
        json.put("allowFlight", allowFlight)
        json.put("bukkitVersion", bukkitVersion)
        json.put("monsterSpawnLimit", monsterSpawnLimit)
        json.put("settingsIp", settingsIp)
        json.put("motd", motd)
        json.put("settingsPort", settingsPort)
        json.put("worldType", worldType)
        json.put("generateStructures", generateStructures)
        json.put("spawnRadius", spawnRadius)
        json.put("viewDistance", viewDistance)
        json.put("worlds", worlds)
        json.put("ip", ip)
        return json
    }
}