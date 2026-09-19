package invirt.mongo.test

import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.MongoCollection
import invirt.data.RecordsPage
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.VersionedDocument
import invirt.mongodb.mongoNow
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.date.shouldBeCloseTo
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.Document
import org.bson.conversions.Bson
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

infix fun TimestampedDocument.shouldBeUpdateOf(other: TimestampedDocument) {
    val thisUpdatedAt = this.updatedAt.truncatedTo(ChronoUnit.MILLIS)
    val otherUpdatedAt = other.updatedAt.truncatedTo(ChronoUnit.MILLIS)
    withClue("$thisUpdatedAt is not after $otherUpdatedAt") {
        thisUpdatedAt.isAfter(otherUpdatedAt) shouldBe true
    }
    withClue("$version is not greater than ${other.version} ") {
        version shouldBeGreaterThan other.version
    }
}

infix fun TimestampedDocument.shouldBeNextUpdateOf(other: TimestampedDocument) {
    val thisUpdatedAt = this.updatedAt.truncatedTo(ChronoUnit.MILLIS)
    val otherUpdatedAt = other.updatedAt.truncatedTo(ChronoUnit.MILLIS)
    withClue("$thisUpdatedAt is not after $otherUpdatedAt") {
        thisUpdatedAt.isAfter(otherUpdatedAt) shouldBe true
    }
    withClue("$version is not ${other.version} + 1") {
        version shouldBe other.version + 1
    }
}

infix fun TimestampedDocument?.shouldBeSameDocument(other: TimestampedDocument) {
    this!!.shouldBeEqualToIgnoringFields(
        other,
        TimestampedDocument::version,
        TimestampedDocument::createdAt,
        TimestampedDocument::updatedAt
    )
}

private fun Document.isAscIndex(field: String): Boolean = (this["key"] as Document)[field] == 1

private fun Document.isDescIndex(field: String): Boolean = (this["key"] as Document)[field] == -1

infix fun MongoCollection<*>.shouldHaveAscIndex(field: String) {
    listIndexes().toList().find { it.isAscIndex(field) } shouldNotBe null
}

infix fun MongoCollection<*>.shouldHaveDescIndex(field: String) {
    listIndexes().toList().find { it.isDescIndex(field) } shouldNotBe null
}

infix fun MongoCollection<*>.shouldNotHaveAscIndex(field: String) {
    listIndexes().toList().find { it.isAscIndex(field) } shouldBe null
}

fun <Doc : TimestampedDocument> MongoCollection<Doc>.shouldHaveTimestampedIndices() {
    shouldHaveAscIndex("version")
    shouldHaveDescIndex("createdAt")
    shouldHaveDescIndex("updatedAt")
}

infix fun <Entity : Any> MongoCollection<Entity>.shouldHaveUniqueIndex(field: String) {
    listIndexes().toList().find { index -> (index["key"] as Document)[field] != null && index["unique"] == true } shouldNotBe null
}

infix fun MongoCollection<*>.shouldNotHaveDescIndex(field: String) {
    listIndexes().toList().find { it.isDescIndex(field) } shouldBe null
}

fun MongoCollection<*>.shouldHaveTextIndex(vararg fields: String) {
    listIndexes().toList().find {
        val indexName = fields.joinToString("_") { field -> "${field}_text" }
        (it["key"] as Document)["_fts"] == "text" && (it["name"] == indexName)
    } shouldNotBe null
}

fun <Doc : VersionedDocument> RecordsPage<Doc>.idsShouldBe(ids: List<String>) {
    this.records.map { it.id } shouldContainExactlyInAnyOrder ids
}

fun <Doc : VersionedDocument> RecordsPage<Doc>.idsShouldBe(vararg ids: String) = idsShouldBe(ids.toList())

fun <Doc : VersionedDocument> RecordsPage<Doc>.idsShouldBeInOrder(ids: List<String>) {
    this.records.map { it.id } shouldContainExactly ids
}

fun <Doc : VersionedDocument> RecordsPage<Doc>.idsShouldBeInOrder(vararg ids: String) = idsShouldBeInOrder(ids.toList())

/**
 * Asserts a TTL index on [field] that expires documents [expireAfterSeconds] seconds after the indexed
 * timestamp, by scanning the collection's indices for a key on [field] carrying a non-null
 * `expireAfterSeconds`. Matches [field]'s key entry regardless of direction - MongoDB does not use the key
 * direction of a TTL index for anything.
 */
fun MongoCollection<*>.shouldHaveTtlIndex(field: String, expireAfterSeconds: Long) {
    val index = listIndexes().toList().find { (it["key"] as Document)[field] != null && it["expireAfterSeconds"] != null }
    withClue("expected a TTL index on '$field'") { index shouldNotBe null }
    withClue("TTL index on '$field' should expire after $expireAfterSeconds seconds") {
        (index!!["expireAfterSeconds"] as Number).toLong() shouldBe expireAfterSeconds
    }
}

/**
 * Asserts a compound UNIQUE index whose key carries exactly [fields] (no more, no fewer) and whose
 * `partialFilterExpression` is exactly [partialFilter]. [shouldHaveUniqueIndex] only checks a single key
 * plus the unique flag, so it silently passes on an index with a wrong or missing partial filter, or one
 * whose key merely contains [fields] among other fields.
 */
fun MongoCollection<*>.shouldHavePartialUniqueIndex(fields: List<String>, partialFilter: Document) {
    val index = listIndexes().toList().find { idx ->
        val key = idx["key"] as Document
        idx["unique"] == true && key.keys == fields.toSet() && idx["partialFilterExpression"] == partialFilter
    }
    withClue("expected a partial unique index on $fields where $partialFilter") { index shouldNotBe null }
}

/**
 * Asserts one index whose key is exactly the compound key built from [keys], in the given field order and
 * directions - built with [invirt.mongodb.ascKey] / [invirt.mongodb.descKey], the same way
 * [invirt.mongodb.compoundIndex] builds a real one, rather than with raw 1/-1 direction ints. At least two
 * keys are required: with only one, this could never distinguish a genuine compound index from the
 * implicit `_id_` index or from a plain single-field index, which would defeat the point of a helper named
 * for a compound one. Field-by-field assertions alone cannot prove a compound index exists either - a
 * timestamped collection's own single-field `createdAt` index already satisfies a check on that field, so a
 * collection with no compound index at all could still pass every individual field check.
 */
fun MongoCollection<*>.shouldHaveCompoundIndex(vararg keys: Bson) {
    require(keys.size >= 2) { "shouldHaveCompoundIndex needs at least 2 keys to assert a compound index, got ${keys.size}" }
    // Compared as ordered entries: BsonDocument equality is set-based, but key order defines a compound index.
    val expected = Indexes.compoundIndex(*keys).toBsonDocument().entries.map { it.key to it.value }
    val index = listIndexes().toList().find { (it["key"] as Document).toBsonDocument().entries.map { e -> e.key to e.value } == expected }
    withClue("expected a compound index with key ${expected.joinToString(", ") { "${it.first}: ${it.second}" }}") { index shouldNotBe null }
}

/**
 * Asserts [this] is a fresh, "roughly now" timestamp - one close to [mongoNow] within [tolerance]. Fails on
 * null.
 *
 * The default tolerance is deliberately generous so a loaded CI box that stalls a worker for a second or
 * two between the write under test and this assertion cannot flake it. A genuinely wrong value (null,
 * epoch, a stale or wrong-field timestamp) is off by far more than the default tolerance and still fails.
 */
fun Instant?.shouldBeAboutNow(tolerance: Duration = 30.seconds) {
    this.shouldNotBeNull().shouldBeCloseTo(mongoNow(), tolerance)
}
