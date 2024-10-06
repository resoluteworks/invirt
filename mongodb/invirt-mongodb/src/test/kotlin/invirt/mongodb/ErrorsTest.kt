package invirt.mongodb

import com.mongodb.MongoException
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.bson.codecs.pojo.annotations.BsonId

class ErrorsTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "duplicate errors" {
            data class TestDocument(
                @BsonId override val id: String,
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.randomTestCollection<TestDocument>()
            val id = uuid7()
            collection.insertOne(TestDocument(id))
            val exception = try {
                collection.insertOne(TestDocument(id))
                null
            } catch (e: Exception) {
                e
            }

            exception shouldNotBe null
            exception.shouldBeInstanceOf<MongoException>()
            exception.isDuplicateError() shouldBe true
        }
    }
}
