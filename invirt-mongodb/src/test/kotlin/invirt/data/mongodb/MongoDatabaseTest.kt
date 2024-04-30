package invirt.data.mongodb

import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoDatabase
import invirt.test.randomDatabase
import invirt.test.testMongoClient
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant
import java.util.*

class MongoDatabaseTest : StringSpec() {

    private val mongoClient = testMongoClient()
    private val mongoDb = mongoClient.randomDatabase()

    init {
        "get collection" {
            @CollectionName("mongodb-get-collection-test")
            data class Entity(
                val name: String,
                @BsonId override val id: String = UUID.randomUUID().toString()
            ) : MongoEntity

            val entity = Entity("John")
            mongoDb.getEntityCollection<Entity>().save(entity)
            mongoDb.getCollection<Entity>("mongodb-get-collection-test").get(entity.id) shouldBe entity
        }

        "get collection creates indexes" {
            @CollectionName("get-collection-creates-indexes-test")
            data class Entity(
                @Indexed
                val ownerId: String,
                @BsonId override val id: String = UUID.randomUUID().toString(),
                override val createdAt: Instant = Instant.now(),
                override var updatedAt: Instant = Instant.now()
            ) : TimestampedEntity

            val collection = mongoDb.getEntityCollection<Entity>()
            collection.listIndexes().toList().firstOrNull { it.getIndexOrder("ownerId") == 1 } shouldNotBe null
            collection.listIndexes().toList().firstOrNull { it.getIndexOrder("createdAt") == 1 } shouldNotBe null
            collection.listIndexes().toList().firstOrNull { it.getIndexOrder("updatedAt") == 1 } shouldNotBe null
        }

        "databaseFromConnectionString" {
            fun testDbNameFromConnectionString(connectionString: String, expectedDbName: String) {
                mockkStatic(MongoClient::database) {
                    val client = spyk(mongoClient)
                    val db = mockk<MongoDatabase>()
                    val dbName = slot<String>()
                    every { client.database(capture(dbName)) } returns db
                    every { db.name } answers { dbName.captured }
                    client.databaseFromConnectionString(connectionString).name shouldBe expectedDbName
                }
            }

            testDbNameFromConnectionString(
                "mongodb://localuser:localpassword@localhost:27017/localdb?authSource=admin",
                "localdb"
            )
            testDbNameFromConnectionString(
                "mongodb://localhost:27017/localdb2?authSource=admin",
                "localdb2"
            )
            testDbNameFromConnectionString(
                "mongodb+srv://user:password@server.host.name.net/database1?retryWrites=true&w=majority",
                "database1"
            )
            testDbNameFromConnectionString(
                "mongodb+srv://server.host.name.net/database2?retryWrites=true&w=majority",
                "database2"
            )
        }
    }

    private fun Document.getIndexOrder(field: String): Int? {
        return (this["key"] as Document)[field] as Int?
    }
}
