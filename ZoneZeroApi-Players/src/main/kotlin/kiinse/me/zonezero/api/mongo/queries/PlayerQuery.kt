package kiinse.me.zonezero.api.mongo.queries

import com.mongodb.client.MongoCollection
import kiinse.me.zonezero.api.core.exceptions.ConfigException
import kiinse.me.zonezero.api.mongo.MongoDb
import kiinse.me.zonezero.api.players.Player
import kiinse.me.zonezero.api.players.enums.TwoFaType
import kiinse.me.zonezero.api.services.ServiceUtils
import org.bson.Document

@Suppress("UNUSED")
object PlayerQuery {

    private var collection: MongoCollection<Document>? = null

    init {
        reload()
    }

    @Throws(ConfigException::class)
    fun reload() {
        collection = MongoDb.getDataBase().getCollection("players")
    }

    private fun playerToDocument(player: Player): Document {
        val query = Document()
        query["_id"] = player.login
        query["password"] = player.password
        query["twoFa"] = player.twoFa
        query["twoFaType"] = player.twoFaType.toString()
        query["lastIp"] = player.lastIp
        return query
    }

    fun createPlayer(player: Player) {
        collection!!.insertOne(playerToDocument(player))
    }

    fun hasPlayer(login: String?): Boolean {
        if (login == null) return false
        val query = Document()
        query["_id"] = login
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasPlayer(login: String, password: String): Boolean {
        val query = Document()
        query["_id"] = login
        val result = collection!!.find(query).first()
        if (result == null || result.isEmpty()) return false
        return ServiceUtils.bcryptCheck(password, parsePlayerResult(result)!!.password!!)
    }

    fun hasPlayerEmail(login: String): Boolean {
        return !(getPlayer(login) ?: return false).twoFa.isNullOrEmpty()
    }

    fun getPlayerEmail(login: String): String {
        return getPlayer(login)?.twoFa ?: ""
    }

    fun hasPlayer(player: Player): Boolean {
        val query = Document()
        query["_id"] = player.login
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun updatePlayer(player: Player): Player {
        val query = Document()
        query["_id"] = player.login
        collection!!.findOneAndReplace(query, playerToDocument(player))
        return player
    }

    fun getPlayer(login: String?): Player? {
        if (login == null) return null
        val query = Document()
        query["_id"] = login
        return parsePlayerResult(collection!!.find(query).first())
    }

    fun getPlayer(login: String?, password: String?): Player? {
        if (login == null || password == null) return null
        val query = Document()
        query["_id"] = login
        val account = parsePlayerResult(collection!!.find(query).first())
        if (account != null && ServiceUtils.bcryptCheck(password, account.password!!)) return account
        return null
    }

    private fun parsePlayerResult(result: Document?): Player? {
        if (result == null || result.keys.size < 3) return null
        return Player(
            result["_id"].toString(),
            result["password"].toString(),
            TwoFaType.valueOf(result["twoFaType"].toString()),
            result["twoFa"].toString(),
            result["lastIp"].toString()
        )
    }

    fun removePlayer(jwt: String) {
        val query = Document()
        query["_id"] = jwt
        collection!!.findOneAndDelete(query)
    }

    fun removePlayer(player: Player) {
        val query = Document()
        query["_id"] = player.login
        collection!!.findOneAndDelete(query)
    }

    fun countPlayers(): Long {
        return collection!!.countDocuments()
    }

    val allPlayers: Set<Player?>
        get() {
            val result = HashSet<Player?>()
            collection!!.find().forEach { result.add(parsePlayerResult(it)) }
            return result
        }
}