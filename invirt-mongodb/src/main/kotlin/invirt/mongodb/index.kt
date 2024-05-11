package invirt.mongodb

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties

private val log = KotlinLogging.logger {}

fun <E : StoredEntity> MongoCollection<E>.createEntityIndexes() {
    createPropertyIndexes(this.documentClass.kotlin)

    // Handling these explicitly, because annotations are not inherited in Kotlin
    createPropertyIndexes(StoredEntity::class)
}

private fun MongoCollection<*>.createPropertyIndexes(cls: KClass<*>) {
    val textIndexedFields = mutableListOf<String>()
    val indexes = mutableListOf<IndexModel>()
    val stringType = String::class.createType()

    cls.memberProperties.forEach { property ->

        // Indexed properties
        property.findAnnotation<Indexed>()?.let { annotation ->
            val fields = if (annotation.fields.isEmpty()) listOf(property.name) else annotation.fields.toList()
            fields.forEach { field ->
                val index = when (annotation.order) {
                    Indexed.Order.ASC -> Indexes.ascending(field)
                    Indexed.Order.DESC -> Indexes.descending(field)
                }
                val indexOptions = IndexOptions()
                if (annotation.caseInsensitive && property.returnType.isSubtypeOf(stringType)) {
                    indexOptions.collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
                }
                indexes.add(IndexModel(index, indexOptions))
                log.info { "Adding ${annotation.order} index for ${cls.simpleName}.${field}" }
            }
        }

        // Text indexed properties need collecting into one index as Mongo only supports one text index
        property.findAnnotation<TextIndexed>()?.let { annotation ->
            val fields = if (annotation.fields.isEmpty()) listOf(property.name) else annotation.fields.toList()
            textIndexedFields.addAll(fields)
        }
    }

    if (textIndexedFields.isNotEmpty()) {
        indexes.add(IndexModel(Indexes.compoundIndex(textIndexedFields.map { Indexes.text(it) })))
        log.info { "Adding text index for ${textIndexedFields.joinToString(", ") { cls.simpleName + "." + it }}" }
    }

    if (indexes.isNotEmpty()) {
        createIndexes(indexes)
        log.info { "Created ${indexes.size} indexes for ${cls.simpleName}" }
    }
}
