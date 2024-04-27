package invirt.data.mongodb

import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.BsonInt64
import org.bson.Document

private val log = KotlinLogging.logger {}

fun mongoClient(connectionString: String): MongoClient {
    log.info { "MongoDB connection string: ${connectionString.replace("://.*@".toRegex(), "://*****@")}" }
    val client = MongoClient.create(connectionString)
    return client
}

fun MongoClient.database(name: String): MongoDatabase {
    val database = getDatabase(name)
    database.runCommand(Document("ping", BsonInt64(1)))
    log.info { "Successfully pinged MongoDB database '${name}'" }
    return database
}

fun MongoClient.databaseFromConnectionString(connectionString: String): MongoDatabase {
    val connectionStr = ConnectionString(connectionString)
    val databaseName = connectionStr.database!!
    return database(databaseName)
}

inline fun <reified T : MongoEntity> MongoDatabase.getEntityCollection(): MongoCollection<T> {
    val collection = getCollection<T>(collectionName<T>())
    collection.createEntityIndexes()
    return collection
}
