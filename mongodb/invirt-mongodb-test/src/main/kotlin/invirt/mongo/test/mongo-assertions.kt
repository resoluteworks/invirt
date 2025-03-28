package invirt.mongo.test

import com.mongodb.kotlin.client.MongoCollection
import invirt.data.RecordsPage
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.VersionedDocument
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.Document
import java.time.temporal.ChronoUnit
import javax.print.Doc

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
