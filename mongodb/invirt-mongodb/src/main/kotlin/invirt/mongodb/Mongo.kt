package invirt.mongodb

import com.mongodb.TransactionOptions
import com.mongodb.WriteConcern
import com.mongodb.kotlin.client.ClientSession
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.BsonInt64
import org.bson.Document
import java.net.URI

private val log = KotlinLogging.logger {}

class Mongo(val connectionString: String) {

    val databaseName: String = URI(connectionString).path.replace("^/".toRegex(), "")

    internal val mongoClient: MongoClient by lazy {
        MongoClient.create(connectionString)
    }

    val database: MongoDatabase by lazy {
        val db = mongoClient.getDatabase(databaseName)
        db.runCommand(Document("ping", BsonInt64(1)))
        log.info { "Successfully pinged MongoDB database '${databaseName}'" }
        db
    }

    init {
        log.debug { "MongoDB connection string: ${connectionString.replace("://.*@".toRegex(), "://*****@")}" }
        if (databaseName.isEmpty()) {
            throw IllegalArgumentException("Database missing from connection string")
        }
    }

    fun <Result> runInTransaction(block: (ClientSession) -> Result): Result {
        val session = mongoClient.startSession()
        return try {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build())
            val result = block(session)
            session.commitTransaction()
            result
        } catch (e: Throwable) {
            log.error(e) { "MongoDB transaction error: ${e.message}" }
            session.abortTransaction()
            throw e
        } finally {
            session.close()
        }
    }

    fun close() {
        mongoClient.close()
    }
}
