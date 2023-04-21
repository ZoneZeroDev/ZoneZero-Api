package kiinse.me.zonezero.api.mongo.queries

import com.mongodb.client.MongoCollection
import kiinse.me.zonezero.api.core.exceptions.ConfigException
import kiinse.me.zonezero.api.mongo.MongoDb
import kiinse.me.zonezero.api.server.QueryServer
import kiinse.me.zonezero.api.server.RegisteredServer
import org.bson.Document
import org.json.JSONObject

object RegisteredServerQuery {

    private var collection: MongoCollection<Document>? = null

    init {
        reload()
    }

    @Throws(ConfigException::class)
    fun reload() {
        collection = MongoDb.getDataBase().getCollection("registeredServers")
    }

    private fun getDocumentFromServer(registeredServer: RegisteredServer): Document {
        val query = Document()
        query["_id"] = registeredServer.getId()
        query["email"] = registeredServer.email
        query["name"] = registeredServer.name
        query["maxPlayers"] = registeredServer.maxPlayers
        query["pluginVersion"] = registeredServer.pluginVersion
        query["allowEnd"] = registeredServer.allowEnd
        query["allowNether"] = registeredServer.allowNether
        query["allowFlight"] = registeredServer.allowFlight
        query["bukkitVersion"] = registeredServer.bukkitVersion
        query["monsterSpawnLimit"] = registeredServer.monsterSpawnLimit
        query["settingsIp"] = registeredServer.settingsIp
        query["motd"] = registeredServer.motd
        query["settingsPort"] = registeredServer.settingsPort
        query["worldType"] = registeredServer.worldType
        query["generateStructures"] = registeredServer.generateStructures
        query["spawnRadius"] = registeredServer.spawnRadius
        query["viewDistance"] = registeredServer.viewDistance
        query["worlds"] = registeredServer.worlds
        query["ip"] = registeredServer.ip
        return query
    }

    fun saveServer(registeredServer: RegisteredServer) {
        if (hasServer(registeredServer)) {
            val query = Document()
            query["_id"] = registeredServer.getId()
            collection!!.replaceOne(query, getDocumentFromServer(registeredServer))
        } else {
            collection!!.insertOne(getDocumentFromServer(registeredServer))
        }
    }

    fun saveServer(queryServer: QueryServer, email: String) {
        saveServer(RegisteredServer(email,
                                    queryServer.name,
                                    queryServer.maxPlayers,
                                    queryServer.pluginVersion,
                                    queryServer.allowEnd,
                                    queryServer.allowNether,
                                    queryServer.allowFlight,
                                    queryServer.bukkitVersion,
                                    queryServer.monsterSpawnLimit,
                                    queryServer.settingsIp,
                                    queryServer.motd,
                                    queryServer.settingsPort,
                                    queryServer.worldType,
                                    queryServer.generateStructures,
                                    queryServer.spawnRadius,
                                    queryServer.viewDistance,
                                    queryServer.worlds,
                                    queryServer.ip))
    }

    fun updateServer(queryServer: QueryServer) {
        if (hasServer(queryServer)) {
            val registeredServer = getServer(queryServer)!!
            saveServer(RegisteredServer(registeredServer.email,
                                        queryServer.name,
                                        queryServer.maxPlayers,
                                        queryServer.pluginVersion,
                                        queryServer.allowEnd,
                                        queryServer.allowNether,
                                        queryServer.allowFlight,
                                        queryServer.bukkitVersion,
                                        queryServer.monsterSpawnLimit,
                                        queryServer.settingsIp,
                                        queryServer.motd,
                                        queryServer.settingsPort,
                                        queryServer.worldType,
                                        queryServer.generateStructures,
                                        queryServer.spawnRadius,
                                        queryServer.viewDistance,
                                        queryServer.worlds,
                                        queryServer.ip))
        }
    }

    fun isValid(ip: String, userEmail: String, id: String): Boolean {
        val query = Document()
        query["_id"] = id
        query["ip"] = ip
        query["email"] = userEmail
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasServer(registeredServer: RegisteredServer): Boolean {
        val query = Document()
        query["_id"] = registeredServer.getId()
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasServer(queryServer: QueryServer): Boolean {
        val query = Document()
        query["_id"] = queryServer.getId()
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun getServer(queryServer: QueryServer): RegisteredServer? {
        val query = Document()
        query["_id"] = queryServer.getId()
        return parseServerResult(collection!!.find(query).first())
    }

    fun getServer(serverId: String): RegisteredServer? {
        val query = Document()
        query["_id"] = serverId
        return parseServerResult(collection!!.find(query).first())
    }

    fun getServersByEmail(email: String): JSONObject {
        val query = Document()
        query["email"] = email
        val json = JSONObject()
        collection!!.find(query).forEach {
            val server = parseServerResult(it)!!
            json.put(server.getId(), server.toJson())
        }
        return json
    }

    fun hasServer(ip: String, email: String): Boolean {
        val query = Document()
        query["ip"] = ip
        query["email"] = email
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseServerResult(result: Document?): RegisteredServer? {
        if (result == null || result.keys.size < 3) return null
        return RegisteredServer(
            result["email"].toString(),
            result["name"].toString(),
            result["maxPlayers"].toString().toInt(),
            result["pluginVersion"].toString(),
            result["allowEnd"].toString().toBoolean(),
            result["allowNether"].toString().toBoolean(),
            result["allowFlight"].toString().toBoolean(),
            result["bukkitVersion"].toString(),
            result["monsterSpawnLimit"].toString().toInt(),
            result["settingsIp"].toString(),
            result["motd"].toString(),
            result["settingsPort"].toString().toInt(),
            result["worldType"].toString(),
            result["generateStructures"].toString().toBoolean(),
            result["spawnRadius"].toString().toInt(),
            result["viewDistance"].toString().toInt(),
            stringToWorlds(result["worlds"] as ArrayList<String>),
            result["ip"].toString())
    }

    fun countServers(): Long {
        return collection!!.countDocuments()
    }

    val allServers: Set<RegisteredServer?>
        get() {
            val result = HashSet<RegisteredServer?>()
            collection!!.find().forEach { result.add(parseServerResult(it)) }
            return result
        }

    fun getAllServersByEmail(email: String): Set<RegisteredServer?> {
        val result = HashSet<RegisteredServer?>()
        val query = Document()
        query["email"] = email
        collection!!.find().forEach { result.add(parseServerResult(it)) }
        return result
    }

    private fun stringToWorlds(worlds: ArrayList<String>): Set<String> {
        val set = HashSet<String>()
        for (world in worlds) {
            set.add(world)
        }
        return set
    }

}