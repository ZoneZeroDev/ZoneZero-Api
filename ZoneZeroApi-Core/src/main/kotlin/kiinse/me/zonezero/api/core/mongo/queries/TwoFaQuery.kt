package kiinse.me.zonezero.api.core.mongo.queries

import com.mongodb.client.MongoCollection
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import kiinse.me.zonezero.api.core.config.Addresses
import kiinse.me.zonezero.api.core.exceptions.ConfigException
import kiinse.me.zonezero.api.core.exceptions.EmailException
import kiinse.me.zonezero.api.core.mongo.MongoDb
import kiinse.me.zonezero.api.core.players.Player
import kiinse.me.zonezero.api.core.players.enums.TwoFaType
import kiinse.me.zonezero.api.core.twofa.TwoFaData
import kiinse.me.zonezero.api.core.twofa.enums.QueryType
import kiinse.me.zonezero.api.core.utils.TwoFaUtils
import org.bson.Document
import org.json.JSONObject
import java.time.Instant
import java.util.HashMap

object TwoFaQuery {

    private var collection: MongoCollection<Document>? = null

    init {
        reload()
    }

    @Throws(ConfigException::class)
    fun reload() {
        collection = MongoDb.getDataBase().getCollection("2faQuery")
    }

    private fun dataToDocument(data: TwoFaData): Document {
        val query = Document()
        query["_id"] = data.code
        query["queryType"] = data.queryType.toString()
        query["twoFaType"] = data.twoFaType.toString()
        query["address"] = data.address
        query["player"] = data.player
        query["data"] = data.data
        query["time"] = data.time.toEpochMilli()
        return query
    }

    fun createQuery(request: HttpRequest<String?>, data: TwoFaData): Boolean {
        removeQuery(data.player)
        val send = TwoFaUtils.post(request, Addresses.EMAIL, "send", getCodeEmail(data))
        if (send.code != 200) throw EmailException(HttpStatus.INTERNAL_SERVER_ERROR, send.body.toString())
        collection!!.insertOne(dataToDocument(data))
        return true
    }

    private fun getCodeEmail(data: TwoFaData): JSONObject {
        val json = JSONObject()
        json.put("address", data.address)
        json.put("subject", "ZoneZero | 2Fa")
        val template = JSONObject()
        template.put("name", "code")
        val hashmap = HashMap<String, String>()
        hashmap["projectTitle"] = "ZoneZero"
        hashmap["label"] = "Action confirm | ${data.queryType.value}"
        hashmap["text"] = "Use this command on the server. If you did not request this code on servers with the ZoneZero plugin, then you can safely delete this message. Code validity time: 10 minutes."
        hashmap["code"] = "/2fa ${data.code}"
        hashmap["footer"] = "You have received this email because we have received a request to confirm some action from the minecraft server with the ZoneZero plugin installed. If you have not done anything that would require this code, then you can safely delete this letter."
        hashmap["url"] = "https://zonezero.dev/"
        template.put("data", hashmap)
        json.put("template", template)
        return json
    }

    fun hasQuery(code: String): Boolean {
        val query = Document()
        query["_id"] = code
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun getQuery(code: String, player: Player): TwoFaData? {
        val query = Document()
        query["_id"] = code
        query["player"] = player.login
        return parseDataResult(collection!!.find(query).first())
    }

    private fun parseDataResult(result: Document?): TwoFaData? {
        if (result == null || result.keys.size < 3) return null
        return TwoFaData(
            result["_id"].toString(),
            QueryType.valueOf(result["queryType"].toString()),
            TwoFaType.valueOf(result["twoFaType"].toString()),
            result["address"].toString(),
            result["player"].toString(),
            result["data"].toString(),
            Instant.ofEpochMilli(result["time"].toString().toLong())
        )
    }

    private fun removeQuery(player: String) {
        val query = Document()
        query["player"] = player
        collection!!.deleteMany(query)
    }
}