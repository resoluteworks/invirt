package invirt.mongodb.cursor

import com.mongodb.kotlin.client.MongoCollection
import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.mongoNow
import invirt.utils.minusDays
import invirt.utils.plusDays
import invirt.utils.uuid7
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import java.time.Instant

class CursorAggregateTest : FreeSpec() {

    private val mongo = testMongo()

    init {
        "should return the correct results for single sort asc" {
            val collection = mongo.randomTestCollection<CursorTestEntity>()
            insertTestData(collection)

            val sortFields = listOf<SortField<CursorTestEntity>>(SortField("identifier".sortAsc()) { it.identifier })
            val pipeline = emptyList<Bson>()
            val page1 = collection.cursorAggregate(pipeline, 3, null, sortFields)

            page1.prevCursor shouldBe null
            page1.nextCursor shouldNotBe null
            page1.data.map { it.identifier } shouldContainExactly listOf("001", "002", "003")

            val page2 = collection.cursorAggregate(pipeline, 3, page1.nextCursor, sortFields)
            page2.prevCursor shouldNotBe null
            page2.nextCursor shouldNotBe null
            page2.data.map { it.identifier } shouldContainExactly listOf("004", "005", "006")

            // Going back with prev cursor should fetch that data
            val page1Again = collection.cursorAggregate(pipeline, 3, page2.prevCursor, sortFields)
            page1Again.prevCursor shouldBe null
            page1Again.nextCursor shouldNotBe null
            page1Again.data.map { it.identifier } shouldContainExactly listOf("001", "002", "003")

            val page2Again = collection.cursorAggregate(pipeline, 3, page1.nextCursor, sortFields)
            page2Again.prevCursor shouldNotBe null
            page2Again.nextCursor shouldNotBe null
            page2Again.data.map { it.identifier } shouldContainExactly listOf("004", "005", "006")

            val page3 = collection.cursorAggregate(pipeline, 3, page2.nextCursor, sortFields)
            page3.prevCursor shouldNotBe null
            page3.nextCursor shouldNotBe null
            page3.data.map { it.identifier } shouldContainExactly listOf("007", "008", "009")

            val page4 = collection.cursorAggregate(pipeline, 3, page3.nextCursor, sortFields)
            page4.prevCursor shouldNotBe null
            page4.nextCursor shouldBe null
            page4.data.map { it.identifier } shouldContainExactly listOf("010")
        }

        "should return the correct results for single sort desc" {
            val collection = mongo.randomTestCollection<CursorTestEntity>()
            insertTestData(collection)

            val sortFields = listOf<SortField<CursorTestEntity>>(SortField("identifier".sortDesc()) { it.identifier })
            val pipeline = emptyList<Bson>()
            val page1 = collection.cursorAggregate(pipeline, 3, null, sortFields)

            page1.prevCursor shouldBe null
            page1.nextCursor shouldNotBe null
            page1.data.map { it.identifier } shouldContainExactly listOf("010", "009", "008")

            val page2 = collection.cursorAggregate(pipeline, 3, page1.nextCursor, sortFields)
            page2.prevCursor shouldNotBe null
            page2.nextCursor shouldNotBe null
            page2.data.map { it.identifier } shouldContainExactly listOf("007", "006", "005")

            // Going back with prev cursor should fetch that data
            val page1Again = collection.cursorAggregate(pipeline, 3, page2.prevCursor, sortFields)
            page1Again.prevCursor shouldBe null
            page1Again.nextCursor shouldNotBe null
            page1Again.data.map { it.identifier } shouldContainExactly listOf("010", "009", "008")

            val page2Again = collection.cursorAggregate(pipeline, 3, page1.nextCursor, sortFields)
            page2Again.prevCursor shouldNotBe null
            page2Again.nextCursor shouldNotBe null
            page2Again.data.map { it.identifier } shouldContainExactly listOf("007", "006", "005")

            val page3 = collection.cursorAggregate(pipeline, 3, page2.nextCursor, sortFields)
            page3.prevCursor shouldNotBe null
            page3.nextCursor shouldNotBe null
            page3.data.map { it.identifier } shouldContainExactly listOf("004", "003", "002")

            val page4 = collection.cursorAggregate(pipeline, 3, page3.nextCursor, sortFields)
            page4.prevCursor shouldNotBe null
            page4.nextCursor shouldBe null
            page4.data.map { it.identifier } shouldContainExactly listOf("001")
        }

        "should return the correct results for custom vs stable sort" {
            val collection = mongo.randomTestCollection<CursorTestEntity>()
            insertTestData(collection)

            val sortFields = listOf<SortField<CursorTestEntity>>(
                SortField("submitted".sortAsc()) { it.submitted },
                SortField("identifier".sortDesc()) { it.identifier }
            )
            val pipeline = emptyList<Bson>()
            val page1 = collection.cursorAggregate(pipeline, 3, null, sortFields)
            page1.prevCursor shouldBe null
            page1.nextCursor shouldNotBe null
            page1.data.map { it.identifier } shouldContainExactly listOf("002", "001", "003")

            val page2 = collection.cursorAggregate(pipeline, 3, page1.nextCursor, sortFields)
            page2.prevCursor shouldNotBe null
            page2.nextCursor shouldNotBe null
            page2.data.map { it.identifier } shouldContainExactly listOf("004", "005", "006")

            val page3 = collection.cursorAggregate(pipeline, 3, page2.nextCursor, sortFields)
            page3.prevCursor shouldNotBe null
            page3.nextCursor shouldNotBe null
            page3.data.map { it.identifier } shouldContainExactly listOf("008", "007", "009")

            val page4 = collection.cursorAggregate(pipeline, 3, page3.nextCursor, sortFields)
            page4.prevCursor shouldNotBe null
            page4.nextCursor shouldBe null
            page4.data.map { it.identifier } shouldContainExactly listOf("010")
        }
    }

    private fun insertTestData(collection: MongoCollection<CursorTestEntity>) {
        val refNow = mongoNow()
        collection.insertMany(
            listOf(
                CursorTestEntity(submitted = refNow.minusDays(2), identifier = "001"),
                CursorTestEntity(submitted = refNow.minusDays(2), identifier = "002"),
                CursorTestEntity(submitted = refNow.minusSeconds(1), identifier = "003"),

                CursorTestEntity(submitted = refNow, identifier = "004"),
                CursorTestEntity(submitted = refNow.plusSeconds(10), identifier = "005"),
                CursorTestEntity(submitted = refNow.plusDays(1), identifier = "006"),

                CursorTestEntity(submitted = refNow.plusDays(2), identifier = "008"),
                CursorTestEntity(submitted = refNow.plusDays(2), identifier = "007"),
                CursorTestEntity(submitted = refNow.plusDays(5), identifier = "009"),

                CursorTestEntity(submitted = refNow.plusDays(6), identifier = "010")
            )
        )
    }
}

data class CursorTestEntity(
    val submitted: Instant,
    val identifier: String,
    @BsonId override val id: String = uuid7(),
    override var version: Long = 0,
    override var createdAt: Instant = mongoNow(),
    override var updatedAt: Instant = mongoNow()
) : TimestampedDocument
