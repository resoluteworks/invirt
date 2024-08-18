package invirt.mongodb

import invirt.randomTestCollection
import invirt.testMongo
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.codecs.pojo.annotations.BsonId

class TransactionTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "tx failure" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()

            shouldThrow<IllegalStateException> {
                mongo.runInTransaction { session ->
                    collection.txInsert(session, TestDocument("Test"))
                    throw IllegalStateException("Unlucky")
                }
            }
            collection.countDocuments() shouldBe 0
        }

        "tx success" {
            data class TestDocument(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val id1 = collection.insert(TestDocument("One")).id
            val id2 = collection.insert(TestDocument("Two")).id
            val id3 = mongo.runInTransaction { session ->
                collection.txDelete(session, id2)
                collection.txDelete(session, "123") // Shouldn't cause a problem as it doesn't exist
                collection.txUpdate(session, collection.get(id1)!!.copy(name = "1"))
                collection.txInsert(session, TestDocument("3")).id
            }

            collection.countDocuments() shouldBe 2
            collection.get(id1)!!.name shouldBe "1"
            collection.get(id3)!!.name shouldBe "3"
        }
    }
}
