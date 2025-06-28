package invirt.mongodb

import com.mongodb.MongoCommandException
import com.mongodb.MongoException
import com.mongodb.MongoWriteException
import com.mongodb.ServerAddress
import com.mongodb.WriteError
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId

class ErrorsTest : StringSpec() {

    private val mongo = testMongo()

    init {

        "duplicate errors should be detected based on exception and error code" {
            MongoCommandException(Document("code", 11000).toBsonDocument(), ServerAddress()).isDuplicateError() shouldBe true
            MongoCommandException(Document("code", 11001).toBsonDocument(), ServerAddress()).isDuplicateError() shouldBe false

            listOf(11000, 11001, 12582).shouldForAll { code ->
                MongoWriteException(
                    WriteError(code, "Duplicate key error", Document().toBsonDocument()),
                    ServerAddress(),
                    emptyList()
                ).isDuplicateError() shouldBe true
            }

            MongoWriteException(
                WriteError(12345, "Some other error", Document().toBsonDocument()),
                ServerAddress(),
                emptyList()
            ).isDuplicateError() shouldBe false
        }

        "duplicate errors should be detected when operating on collections" {
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
