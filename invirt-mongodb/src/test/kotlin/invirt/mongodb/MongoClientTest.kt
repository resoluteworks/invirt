package invirt.mongodb

import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId

class MongoClientTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "a session started from mongoClient runs a transaction on the database's collections" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            mongo.mongoClient.startSession().use { session ->
                session.startTransaction()
                collection.txInsert(session, TestDocument("committed"))
                session.commitTransaction()
            }
            mongo.mongoClient.startSession().use { session ->
                session.startTransaction()
                collection.txInsert(session, TestDocument("aborted"))
                session.abortTransaction()
            }

            collection.find().toList().map { it.name } shouldBe listOf("committed")
        }
    }
}
