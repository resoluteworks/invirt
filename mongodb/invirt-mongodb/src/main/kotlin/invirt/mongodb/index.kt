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

fun String.indexAsc(unique: Boolean = false, caseInsensitive: Boolean = false): IndexModel =
    IndexModel(Indexes.ascending(this), indexOptions(unique, caseInsensitive))

fun String.indexDesc(unique: Boolean = false, caseInsensitive: Boolean = false): IndexModel =
    IndexModel(Indexes.descending(this), indexOptions(unique, caseInsensitive))

fun <Value : Any> KProperty<Value?>.indexAsc(unique: Boolean = false, caseInsensitive: Boolean = false): IndexModel =
    name.indexAsc(unique, caseInsensitive)

fun <Value : Any> KProperty<Value?>.indexDesc(unique: Boolean = false, caseInsensitive: Boolean = false): IndexModel =
    name.indexDesc(unique, caseInsensitive)

fun textIndex(vararg fields: String): IndexModel = IndexModel(Indexes.compoundIndex(fields.map { Indexes.text(it) }))

fun MongoCollection<*>.createIndices(vararg indexModels: IndexModel) {
    val indexes = mutableListOf<IndexModel>()
    indexes.addAll(indexModels)

    if (documentClass.kotlin.isSubclassOf(VersionedDocument::class)) {
        indexes.add(VersionedDocument::version.indexAsc(unique = false, caseInsensitive = false))
    }

    if (documentClass.kotlin.isSubclassOf(TimestampedDocument::class)) {
        indexes.add(TimestampedDocument::createdAt.indexDesc(unique = false, caseInsensitive = false))
        indexes.add(TimestampedDocument::updatedAt.indexDesc(unique = false, caseInsensitive = false))
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

private fun indexOptions(unique: Boolean, caseInsensitive: Boolean): IndexOptions {
    val indexOptions = IndexOptions()
    if (unique) {
        indexOptions.unique(true)
    }
    if (caseInsensitive) {
        indexOptions.collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
    }
    return indexOptions
}
