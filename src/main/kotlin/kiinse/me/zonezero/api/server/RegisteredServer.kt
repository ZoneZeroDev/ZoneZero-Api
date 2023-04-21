package kiinse.me.zonezero.api.server

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

    fun getId(): String {
        return name + ip + settingsPort + maxPlayers + bukkitVersion + email
    }
}