package kiinse.me.zonezero.api.mongo.queries

import com.mongodb.client.MongoCollection
import kiinse.me.zonezero.api.core.exceptions.ConfigException
import kiinse.me.zonezero.api.mongo.MongoDb
import org.bson.Document

object RegisteredServerQuery {

    private var collection: MongoCollection<Document>? = null

    init {
        reload()
    }

    @Throws(ConfigException::class)
    fun reload() {
        collection = MongoDb.getDataBase().getCollection("registeredServers")
    }

    fun hasServer(ip: String, email: String): Boolean {
        val query = Document()
        query["ip"] = ip
        query["email"] = email
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

}