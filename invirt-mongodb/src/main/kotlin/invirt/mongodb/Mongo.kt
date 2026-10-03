package invirt.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
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

/**
 * Represents a connection to a MongoDB database.
 * @param connectionString The connection string to the MongoDB database.
 * @param configureClient Applied to the client's settings after the connection string, for what a connection string
 * cannot express, such as a command listener a test installs.
 * @throws IllegalArgumentException If the connection string is missing the database name.
 */
class Mongo(
    val connectionString: String,
    private val configureClient: MongoClientSettings.Builder.() -> Unit = {}
) {

    val databaseName: String = URI(connectionString).path.replace("^/".toRegex(), "")

    /**
     * The driver client behind [database]. A session works only with collections of the client that started it, so
     * code that opens its own sessions for this database (a migration runner, a job framework) is built from this
     * client rather than from a second one created for the same connection string.
     */
    val mongoClient: MongoClient by lazy {
        MongoClient.create(
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(connectionString))
                .apply(configureClient)
                .build()
        )
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

    /**
     * Runs the specified [block] in a MongoDB transaction.
     */
    fun <Result> runInTransaction(block: (ClientSession) -> Result): Result {
        val session = mongoClient.startSession()
        return try {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build())
            val result = block(session)
            session.commitTransaction()
            result
        } catch (e: Exception) {
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
