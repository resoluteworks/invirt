package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.conversions.Bson
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
 * Creates an ascending index on the specified fields, in the order given.
 * A single field is the same index as [asc]; two or more make a compound index.
 *
 * @param fields The fields to index, as strings.
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the index.
 */
fun ascIndex(vararg fields: String, options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.ascending(fields.toList()), options(IndexOptions()))

/**
 * Creates an ascending index on the specified properties, in the order given.
 * A single property is the same index as [asc]; two or more make a compound index.
 *
 * @param fields The properties to index.
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the index.
 */
fun ascIndex(vararg fields: KProperty<*>, options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.ascending(fields.map { it.name }), options(IndexOptions()))

/**
 * Creates a compound index on the specified [keys], for an index whose fields do not all run in the
 * same direction. Each key comes from [ascKey] or [descKey], which are the key-level counterparts of
 * [asc] and [desc]: they return one key of an index rather than a whole index.
 *
 * ```kotlin
 * compoundIndex(Event::topic.ascKey(), Event::createdAt.descKey())
 * ```
 *
 * @param keys The index keys, in order.
 * @param options A lambda to configure the IndexOptions.
 * @return An IndexModel representing the compound index.
 */
fun compoundIndex(vararg keys: Bson, options: IndexOptions.() -> IndexOptions = { this }): IndexModel =
    IndexModel(Indexes.compoundIndex(*keys), options(IndexOptions()))

/**
 * An ascending key on this field, for use in [compoundIndex].
 */
fun String.ascKey(): Bson = Indexes.ascending(this)

/**
 * A descending key on this field, for use in [compoundIndex].
 */
fun String.descKey(): Bson = Indexes.descending(this)

/**
 * An ascending key on this property, for use in [compoundIndex].
 */
fun KProperty<*>.ascKey(): Bson = this.name.ascKey()

/**
 * A descending key on this property, for use in [compoundIndex].
 */
fun KProperty<*>.descKey(): Bson = this.name.descKey()

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
