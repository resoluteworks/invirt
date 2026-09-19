package invirt.mongo.test

import invirt.mongodb.VersionedDocument
import invirt.mongodb.asc
import invirt.mongodb.ascKey
import invirt.mongodb.compoundIndex
import invirt.mongodb.createIndices
import invirt.mongodb.descKey
import invirt.mongodb.mongoNow
import invirt.utils.uuid7
import io.kotest.assertions.shouldFail
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class MongoAssertionsTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "shouldHaveTtlIndex passes on a matching TTL index and fails on a missing or mismatched one" {
            data class Doc(
                val expiresAt: Instant,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.database.getCollection<Doc>(uuid7())
            collection.createIndices("expiresAt".asc { expireAfter(3600L, TimeUnit.SECONDS) })

            collection.shouldHaveTtlIndex("expiresAt", expireAfterSeconds = 3600)

            shouldFail { collection.shouldHaveTtlIndex("neverIndexed", expireAfterSeconds = 3600) }
                .message shouldContain "expected a TTL index on 'neverIndexed'"

            shouldFail { collection.shouldHaveTtlIndex("expiresAt", expireAfterSeconds = 60) }
                .message shouldContain "should expire after 60 seconds"
        }

        "shouldHavePartialUniqueIndex passes on a matching partial unique index and fails on a wrong filter or an over-wide key" {
            data class Doc(
                val organisationId: String,
                val invitedEmail: String,
                val status: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.database.getCollection<Doc>(uuid7())
            val pendingFilter = Document("status", "PENDING")
            collection.createIndices(
                compoundIndex(Doc::organisationId.ascKey(), Doc::invitedEmail.ascKey()) {
                    unique(true).partialFilterExpression(pendingFilter)
                },
                // A wider unique+partial index over an extra field, so the assertion is proven not to pass
                // merely because the named fields are present among others.
                compoundIndex(Doc::organisationId.ascKey(), Doc::invitedEmail.ascKey(), Doc::status.ascKey()) {
                    unique(true).partialFilterExpression(Document("status", "ACCEPTED"))
                }
            )

            collection.shouldHavePartialUniqueIndex(listOf("organisationId", "invitedEmail"), pendingFilter)

            shouldFail {
                collection.shouldHavePartialUniqueIndex(listOf("organisationId", "invitedEmail"), Document("status", "ACCEPTED"))
            }.message shouldContain "expected a partial unique index"

            shouldFail {
                collection.shouldHavePartialUniqueIndex(listOf("organisationId"), pendingFilter)
            }.message shouldContain "expected a partial unique index"
        }

        "shouldHaveCompoundIndex passes on the exact compound key and fails on a reordered, differently-directed or missing one" {
            data class Doc(
                val organisationId: String,
                val createdAt: Instant,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val collection = mongo.database.getCollection<Doc>(uuid7())
            collection.createIndices(compoundIndex(Doc::organisationId.ascKey(), Doc::createdAt.descKey()))

            collection.shouldHaveCompoundIndex(Doc::organisationId.ascKey(), Doc::createdAt.descKey())

            shouldFail {
                collection.shouldHaveCompoundIndex(Doc::createdAt.descKey(), Doc::organisationId.ascKey())
            }.message shouldContain "expected a compound index with key"

            shouldFail {
                collection.shouldHaveCompoundIndex(Doc::organisationId.ascKey(), Doc::createdAt.ascKey())
            }.message shouldContain "expected a compound index with key"

            shouldThrowWithMessage<IllegalArgumentException>(
                "shouldHaveCompoundIndex needs at least 2 keys to assert a compound index, got 1"
            ) {
                collection.shouldHaveCompoundIndex(Doc::organisationId.ascKey())
            }
        }

        "shouldBeAboutNow accepts a fresh timestamp and rejects a stale or null one" {
            mongoNow().shouldBeAboutNow()
            Instant.now().minusSeconds(5).shouldBeAboutNow()
            Instant.now().shouldBeAboutNow(5.seconds)

            shouldFail { Instant.now().minus(1, ChronoUnit.HOURS).shouldBeAboutNow() }
            shouldFail { Instant.now().minus(1, ChronoUnit.HOURS).shouldBeAboutNow(1.hours - 1.seconds) }

            shouldFail { (null as Instant?).shouldBeAboutNow() }
        }
    }
}
