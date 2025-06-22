package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KProperty
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

/**
 * Creates an ascending or descending index on a single field.
 * The field can be specified as a string or a KProperty.
 *
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the index.
 */
fun String.asc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.ascending(this), options(IndexOptions()))

/**
 * Creates a descending index on a single field.
 * The field can be specified as a string or a KProperty.
 *
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the index.
 */
fun String.desc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.descending(this), options(IndexOptions()))

/**
 *  Creates an ascending or descending index on a KProperty.
 * The property name is used to create the index.
 *
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the index.
 */
fun KProperty<*>.asc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel = this.name.asc(options)

/**
 * Creates a descending index on a KProperty.
 * The property name is used to create the index.
 *
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the index.
 */
fun KProperty<*>.desc(options: IndexOptions.() -> IndexOptions = { this }): IndexModel = this.name.desc(options)

/**
 * Creates a text index on the specified fields.
 * The fields can be specified as strings or KProperties.
 *
 * @param fields The fields to create the text index on.
 * @return An IndexModel representing the text index.
 */
fun textIndex(vararg fields: String): IndexModel = IndexModel(Indexes.compoundIndex(fields.map { Indexes.text(it) }))

/**
 * Creates indices for the collection using the provided index models.
 * This is a convenience function that wraps the `createIndexes` method of
 * the MongoDB collection to allow the user of vararg syntax.
 * Logs the creation of indices with the collection name and count of indices.
 *
 * @param indexModels The index models to create.
 */
fun MongoCollection<*>.createIndices(vararg indexModels: IndexModel) {
    val durationMs = measureTimeMillis {
        createIndexes(indexModels.toList())
    }
    log.info { "Created ${indexModels.size} indices for collection ${this.namespace.collectionName} in $durationMs ms" }
}

/**
 * Adds a collation to the index options for case-insensitive text search.
 *
 * @param locale The locale to use for the collation (default is "en").
 * @param strength The collation strength (default is CollationStrength.TERTIARY).
 * @return The updated IndexOptions with the specified collation.
 */
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
