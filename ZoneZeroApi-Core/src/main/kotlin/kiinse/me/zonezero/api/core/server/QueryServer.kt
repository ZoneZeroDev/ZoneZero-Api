package kiinse.me.zonezero.api.core.server

import kiinse.me.zonezero.api.core.mongo.queries.QueryServerQuery
import org.json.JSONObject

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
    val worlds: Set<Any>,
    val ip: String) {

    companion object {
        fun valueOf(code: String): QueryServer? {
            return QueryServerQuery.getServer(code)
        }
    }

    fun getId(): String {
        return name + ip + settingsPort + maxPlayers + bukkitVersion
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("_id", getId())
        json.put("name", name)
        json.put("maxPlayers", maxPlayers)
        json.put("pluginVersion", pluginVersion)
        json.put("code", code)
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