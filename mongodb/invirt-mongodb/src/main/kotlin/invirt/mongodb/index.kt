package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

private val log = KotlinLogging.logger {}

fun String.asc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.ascending(this), options(IndexOptions()))

fun String.desc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.descending(this), options(IndexOptions()))

fun KProperty<*>.asc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel = this.name.asc(options)
fun KProperty<*>.desc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel = this.name.desc(options)

fun textIndex(vararg fields: String): IndexModel = IndexModel(Indexes.compoundIndex(fields.map { Indexes.text(it) }))

fun MongoCollection<*>.createIndices(vararg indexModels: IndexModel) {
    val indexes = mutableListOf<IndexModel>()
    indexes.addAll(indexModels)

    if (documentClass.kotlin.isSubclassOf(VersionedDocument::class)) {
        indexes.add(VersionedDocument::version.asc())
    }

    if (documentClass.kotlin.isSubclassOf(TimestampedDocument::class)) {
        indexes.add(TimestampedDocument::createdAt.desc())
        indexes.add(TimestampedDocument::updatedAt.desc())
    }

    val collectionName = this.namespace.collectionName
    log.atInfo {
        message = "Creating indexes for collection"
        payload = mapOf(
            "collection" to collectionName,
            "count" to indexes.size,
            "indexes" to indexes.map { it.keys }
        )
    }
    createIndexes(indexes)
}

fun IndexOptions.caseInsensitive(
    locale: String = "en",
    strength: CollationStrength = CollationStrength.TERTIARY
): IndexOptions {
    val collation = Collation.builder()
        .locale(locale)
        .collationStrength(strength)
        .caseLevel(false)
        .build()
    return this.collation(collation)
}
