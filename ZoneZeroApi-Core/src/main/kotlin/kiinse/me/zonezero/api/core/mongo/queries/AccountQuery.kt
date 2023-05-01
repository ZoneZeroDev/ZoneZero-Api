package kiinse.me.zonezero.api.core.mongo.queries

import com.mongodb.client.MongoCollection
import kiinse.me.zonezero.api.core.exceptions.ConfigException
import kiinse.me.zonezero.api.core.mongo.MongoDb
import kiinse.me.zonezero.api.core.utils.Utils
import kiinse.me.zonezero.api.core.security.Account
import kiinse.me.zonezero.api.core.security.enums.AccountType
import org.bson.Document

@Suppress("UNUSED")
object AccountQuery {

    private var collection: MongoCollection<Document>? = null

    init {
        reload()
    }

    @Throws(ConfigException::class)
    fun reload() {
        collection = MongoDb.getDataBase().getCollection("accounts")
    }

    private fun accountToDocument(account: Account): Document {
        val query = Document()
        query["_id"] = account.email
        query["jwt"] = account.jwt
        query["password"] = account.password
        query["name"] = account.username
        query["type"] = account.type.toString()
        query["expiresAt"] = account.expiresAtString()
        return query
    }

    fun createAccount(account: Account) {
        collection!!.insertOne(accountToDocument(account))
    }

    fun hasAccount(jwt: String): Boolean {
        val query = Document()
        query["jwt"] = jwt
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasLogin(login: String): Boolean {
        val query = Document()
        query["name"] = login
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasEmail(email: String): Boolean {
        val query = Document()
        query["email"] = email
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun hasAccount(account: Account): Boolean {
        val query = Document()
        query["jwt"] = account.jwt
        val result = collection!!.find(query).first()
        return result != null && !result.isEmpty()
    }

    fun updateAccount(account: Account): Account {
        val query = Document()
        query["jwt"] = account.jwt
        collection!!.findOneAndReplace(query, accountToDocument(account))
        return account
    }

    fun regenToken(oldAccount: Account, newAccount: Account): Account {
        val query = Document()
        query["_id"] = oldAccount.email
        collection!!.findOneAndReplace(query, accountToDocument(newAccount))
        return newAccount
    }

    fun getAccount(jwt: String): Account? {
        val query = Document()
        query["jwt"] = jwt
        return parseAccountResult(collection!!.find(query).first())
    }

    fun getAccountByName(name: String): Account? {
        val query = Document()
        query["name"] = name
        return parseAccountResult(collection!!.find(query).first())
    }

    fun getAccountByNamePass(name: String, password: String): Account? {
        val query = Document()
        query["name"] = name
        val account = parseAccountResult(collection!!.find(query).first()) ?: return null
        if (account.checkPassword(password)) return account
        return null
    }

    fun getAccountByEmailPass(email: String, password: String): Account? {
        val query = Document()
        query["email"] = email
        val account = parseAccountResult(collection!!.find(query).first()) ?: return null
        if (account.checkPassword(password)) return account
        return null
    }

    private fun parseAccountResult(result: Document?): Account? {
        if (result == null || result.keys.size < 3) return null
        return Account(result["name"].toString(),
            result["_id"].toString(),
            result["password"].toString(),
            result["jwt"].toString(),
            AccountType.valueOf(result["type"].toString()))
    }

    fun removeAccount(jwt: String) {
        val query = Document()
        query["jwt"] = jwt
        collection!!.findOneAndDelete(query)
    }

    fun removeAccount(account: Account) {
        val query = Document()
        query["jwt"] = account.jwt
        collection!!.findOneAndDelete(query)
    }

    fun countAccounts(): Long {
        return collection!!.countDocuments()
    }

    val allAccounts: Set<Account?>
        get() {
            val result = HashSet<Account?>()
            collection!!.find().forEach { result.add(parseAccountResult(it)) }
            return result
        }
}