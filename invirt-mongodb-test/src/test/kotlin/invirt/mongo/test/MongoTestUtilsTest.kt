package invirt.mongo.test

import invirt.mongodb.VersionedDocument
import invirt.mongodb.insert
import invirt.mongodb.mongoEq
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId

class MongoTestUtilsTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "clearCollections empties every collection except the kept rows and the skipped collection names" {
            data class Doc(
                val createdBy: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collectionA = mongo.database.getCollection<Doc>(uuid7())
            val collectionB = mongo.database.getCollection<Doc>(uuid7())
            val mongockCollection = mongo.database.getCollection<Doc>("mongockChangeLog")

            val kept = collectionA.insert(Doc(createdBy = "data-bootstrap"))
            collectionA.insert(Doc(createdBy = "user:1"))
            collectionB.insert(Doc(createdBy = "user:2"))
            mongockCollection.insert(Doc(createdBy = "user:3"))

            mongo.clearCollections(keep = "createdBy".mongoEq("data-bootstrap"))

            collectionA.find().toList().map { it.id } shouldBe listOf(kept.id)
            collectionB.countDocuments() shouldBe 0
            mongockCollection.countDocuments() shouldBe 1
        }

        "clearCollections with no keep filter deletes every document" {
            data class Doc(
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.database.getCollection<Doc>(uuid7())
            collection.insert(Doc())
            collection.insert(Doc())

            mongo.clearCollections()

            collection.countDocuments() shouldBe 0
        }
    }
}
