package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.ClientSession
import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KProperty1

private val log = KotlinLogging.logger {}

fun String.indexAsc(caseInsensitive: Boolean = false): IndexModel = IndexModel(Indexes.ascending(this), indexOptions(caseInsensitive))

fun String.indexDesc(caseInsensitive: Boolean = false): IndexModel = IndexModel(Indexes.descending(this), indexOptions(caseInsensitive))

fun textIndex(vararg fields: String): IndexModel = IndexModel(Indexes.compoundIndex(fields.map { Indexes.text(it) }))

fun indexAsc(vararg fields: String, caseInsensitive: Boolean = false): List<IndexModel> = fields.map { it.indexAsc(caseInsensitive) }

fun indexDesc(vararg fields: String, caseInsensitive: Boolean = false): List<IndexModel> = fields.map { it.indexDesc(caseInsensitive) }

fun <E : StoredEntity> MongoCollection<E>.createIndexes(
    clientSession: ClientSession? = null,
    build: IndexesBuilder.() -> Unit
) {
    val indexes = buildIndexes(this.namespace.collectionName, build)
    if (clientSession != null) {
        createIndexes(clientSession, indexes)
    } else {
        createIndexes(indexes)
    }
}

/**
 * For backwards compatibility with the Java driver
 */
fun com.mongodb.client.MongoCollection<*>.createIndexes(
    clientSession: com.mongodb.client.ClientSession? = null,
    build: IndexesBuilder.() -> Unit
) {
    val indexes = buildIndexes(this.namespace.collectionName, build)
    if (clientSession != null) {
        createIndexes(clientSession, indexes)
    } else {
        createIndexes(indexes)
    }
}

private fun buildIndexes(collectionName: String, build: IndexesBuilder.() -> Unit): List<IndexModel> {
    val indexesBuilder = IndexesBuilder()
    indexesBuilder.asc(StoredEntity::version)
    indexesBuilder.desc(StoredEntity::createdAt)
    indexesBuilder.desc(StoredEntity::updatedAt)
    indexesBuilder.build()
    log.atInfo {
        message = "Creating indexes for collection"
        payload = mapOf(
            "collection" to collectionName,
            "count" to indexesBuilder.indexes.size,
            "indexes" to indexesBuilder.indexes.map { it.keys }
        )
    }
    return indexesBuilder.indexes
}

private fun indexOptions(caseInsensitive: Boolean): IndexOptions {
    val indexOptions = IndexOptions()
    if (caseInsensitive) {
        indexOptions.collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
    }
    return indexOptions
}

class IndexesBuilder {
    private var textIndexAdded = false
    internal var indexes: MutableList<IndexModel> = mutableListOf()

    fun asc(vararg fields: String, caseInsensitive: Boolean = false) {
        indexes.addAll(indexAsc(*fields, caseInsensitive = caseInsensitive))
    }

    fun desc(vararg fields: String, caseInsensitive: Boolean = false) {
        indexes.addAll(indexDesc(*fields, caseInsensitive = caseInsensitive))
    }

    fun text(vararg fields: String) {
        if (textIndexAdded) {
            throw IllegalStateException("Text index already added for this collection")
        }
        indexes.add(textIndex(*fields))
        textIndexAdded = true
    }

    fun <E : StoredEntity> asc(vararg properties: KProperty1<E, *>, caseInsensitive: Boolean = false) {
        asc(*properties.map { it.name }.toTypedArray(), caseInsensitive = caseInsensitive)
    }

    fun <E : StoredEntity> desc(vararg properties: KProperty1<E, *>, caseInsensitive: Boolean = false) {
        desc(*properties.map { it.name }.toTypedArray(), caseInsensitive = caseInsensitive)
    }

    fun <E : StoredEntity> text(vararg properties: KProperty1<E, String>) {
        text(*properties.map { it.name }.toTypedArray())
    }
}
