package invirt.mongodb

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Variable
import invirt.mongo.test.randomTestCollection
import invirt.mongo.test.testMongo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId

class AggregateExpressionsTest : StringSpec() {

    private val mongo = testMongo()

    init {
        "mongoLookupField - emitted expression" {
            mongoLookupField("lookup") shouldBe Document("\$arrayElemAt", listOf("\$lookup", 0))

            mongoLookupField("lookup", "name") shouldBe Document("\$arrayElemAt", listOf("\$lookup.name", 0))

            mongoLookupField("lookup", "counts.total") shouldBe Document("\$arrayElemAt", listOf("\$lookup.counts.total", 0))

            mongoLookupField("lookup", "count", 0) shouldBe Document(
                "\$ifNull",
                listOf(Document("\$arrayElemAt", listOf("\$lookup.count", 0)), 0)
            )

            mongoLookupField("lookup", "name", "Unknown organisation") shouldBe Document(
                "\$ifNull",
                listOf(Document("\$arrayElemAt", listOf("\$lookup.name", 0)), "Unknown organisation")
            )

            mongoLookupField("lookup", default = false) shouldBe Document(
                "\$ifNull",
                listOf(Document("\$arrayElemAt", listOf("\$lookup", 0)), false)
            )
        }

        "mongoLookupField - reading a looked up scalar" {
            data class Organisation(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            data class Project(
                val title: String,
                val organisationId: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val organisations = mongo.randomTestCollection<Organisation>()
            val projects = mongo.randomTestCollection<Project>()

            val organisation = organisations.insert(Organisation("Resolute"))
            projects.insert(Project("Known", organisation.id))
            projects.insert(Project("Orphaned", "gone"))

            val rows = projects.withDocumentClass<Document>()
                .aggregate(
                    listOf(
                        Aggregates.lookup(
                            organisations.namespace.collectionName,
                            Project::organisationId.name,
                            "_id",
                            "organisationLookup"
                        ),
                        Aggregates.addFields(
                            Field("withDefault", mongoLookupField("organisationLookup", Organisation::name.name, "Unknown")),
                            Field("withoutDefault", mongoLookupField("organisationLookup", Organisation::name.name)),
                            Field("wholeDocument", mongoLookupField("organisationLookup"))
                        ),
                        Aggregates.sort(Sorts.ascending(Project::title.name))
                    )
                ).toList()

            rows.map { it.getString("title") } shouldContainExactly listOf("Known", "Orphaned")

            val known = rows[0]
            known.getString("withDefault") shouldBe "Resolute"
            known.getString("withoutDefault") shouldBe "Resolute"
            (known["wholeDocument"] as Document).getString("_id") shouldBe organisation.id

            // An empty lookup takes the default, and without one the field is simply missing
            val orphaned = rows[1]
            orphaned.getString("withDefault") shouldBe "Unknown"
            orphaned.containsKey("withoutDefault") shouldBe false
            orphaned.containsKey("wholeDocument") shouldBe false
        }

        "mongoCorrelatedEq - emitted expression" {
            mongoCorrelatedEq("userId", "consentUserId") shouldBe Document("\$eq", listOf("\$userId", "\$\$consentUserId"))
        }

        "mongoCorrelatedEq - matching a correlated lookup" {
            data class Event(
                val userId: String,
                val granted: Boolean,
                val sequence: Int,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            data class User(
                val name: String,
                @BsonId override val id: String = uuid7(),
                override var version: Long = 0
            ) : VersionedDocument

            val events = mongo.randomTestCollection<Event>()
            val users = mongo.randomTestCollection<User>()

            val granted = users.insert(User("Granted"))
            val revoked = users.insert(User("Revoked"))
            users.insert(User("Silent"))

            events.insert(Event(granted.id, false, 1))
            events.insert(Event(granted.id, true, 2))
            events.insert(Event(revoked.id, true, 1))
            events.insert(Event(revoked.id, false, 2))

            val rows = users.withDocumentClass<Document>()
                .aggregate(
                    listOf(
                        Aggregates.lookup(
                            events.namespace.collectionName,
                            listOf(Variable("consentUserId", "\$_id")),
                            listOf(
                                Aggregates.match(Filters.expr(mongoCorrelatedEq(Event::userId.name, "consentUserId"))),
                                Aggregates.sort(Sorts.descending(Event::sequence.name)),
                                Aggregates.limit(1)
                            ),
                            "latestEvent"
                        ),
                        Aggregates.addFields(
                            Field(
                                "optedIn",
                                Document("\$eq", listOf(mongoLookupField("latestEvent", Event::granted.name), true))
                            )
                        ),
                        Aggregates.sort(Sorts.ascending(User::name.name))
                    )
                ).toList()

            rows.map { it.getString("name") } shouldContainExactly listOf("Granted", "Revoked", "Silent")
            rows.map { it.getBoolean("optedIn") } shouldContainExactly listOf(true, false, false)
        }
    }
}
