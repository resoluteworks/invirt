package invirt.mongo.test

import invirt.mongodb.VersionedDocument
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import org.bson.codecs.pojo.annotations.BsonId

class CollectionTestUtilsTest : StringSpec() {

    init {
        "spy collection should override the original collection's behavior" {
            data class TestDocument(
                val index: Int,
                val type: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val mongo = testMongo().spyCollection<TestDocument>("spy-collection-test") {
                every { it.find() } throws RuntimeException("I cannot find anything!")
            }

            shouldThrowWithMessage<RuntimeException>("I cannot find anything!") {
                mongo.database.getCollection<TestDocument>("spy-collection-test").find()
            }
        }
    }
}
