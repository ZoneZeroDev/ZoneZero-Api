package kiinse.me.zonezero.api.mongo.queries

import com.mongodb.client.MongoCollection
import kiinse.me.zonezero.api.core.exceptions.ConfigException
import kiinse.me.zonezero.api.mongo.MongoDb
import kiinse.me.zonezero.api.server.QueryServer
import org.bson.Document

object QueryServerQuery {

    private var collection: MongoCollection<Document>? = null

    init {
        reload()
    }

    @Throws(ConfigException::class)
    fun reload() {
        collection = MongoDb.getDataBase().getCollection("queryServers")
    }

    private fun getDocumentFromServer(serverQuery: QueryServer): Document {
        val query = Document()
        query["_id"] = serverQuery.getId()
        query["name"] = serverQuery.name
        query["maxPlayers"] = serverQuery.maxPlayers
        query["pluginVersion"] = serverQuery.pluginVersion
        query["code"] = serverQuery.code
        query["allowEnd"] = serverQuery.allowEnd
        query["allowNether"] = serverQuery.allowNether
        query["allowFlight"] = serverQuery.allowFlight
        query["bukkitVersion"] = serverQuery.bukkitVersion
        query["monsterSpawnLimit"] = serverQuery.monsterSpawnLimit
        query["settingsIp"] = serverQuery.settingsIp
        query["motd"] = serverQuery.motd
        query["settingsPort"] = serverQuery.settingsPort
        query["worldType"] = serverQuery.worldType
        query["generateStructures"] = serverQuery.generateStructures
        query["spawnRadius"] = serverQuery.spawnRadius
        query["viewDistance"] = serverQuery.viewDistance
        query["worlds"] = serverQuery.worlds
        query["ip"] = serverQuery.ip
        return query
    }

    fun saveServer(serverQuery: QueryServer) {
        if (hasServer(serverQuery)) {
            val query = Document()
            query["_id"] = serverQuery.getId()
            collection!!.replaceOne(query, getDocumentFromServer(serverQuery))
        } else {
            collection!!.insertOne(getDocumentFromServer(serverQuery))
        }
    }

    fun hasServer(serverQuery: QueryServer): Boolean {
        val query = Document()
        query["_id"] = serverQuery.getId()
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasServer(code: String): Boolean {
        val query = Document()
        query["code"] = code
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun getServer(code: String): QueryServer? {
        val query = Document()
        query["code"] = code
        return parseServerResult(collection!!.find(query).first())
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseServerResult(result: Document?): QueryServer? {
        if (result == null || result.keys.size < 3) return null
        return QueryServer(
            result["name"].toString(),
            result["maxPlayers"].toString().toInt(),
            result["code"].toString(),
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

    fun removeServer(serverQuery: QueryServer) {
        val query = Document()
        query["_id"] = serverQuery.getId()
        collection!!.findOneAndDelete(query)
    }

    fun removeServer(id: String) {
        val query = Document()
        query["_id"] = id
        collection!!.findOneAndDelete(query)
    }

    fun countServers(): Long {
        return collection!!.countDocuments()
    }

    val allServers: Set<QueryServer?>
        get() {
            val result = HashSet<QueryServer?>()
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