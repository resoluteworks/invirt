package invirt.mongodb

import com.mongodb.MongoClientSettings
import org.bson.BsonDocumentReader
import org.bson.Document
import org.bson.codecs.DecoderContext
import kotlin.reflect.KClass

/**
 * Deserializes the list of [Document] to a list of objects with the specified [Doc] type
 * using the default Mongo codec registry.
 */
inline fun <reified Doc : Any> List<Document>.mongoDeserializeWith(): List<Doc> = mongoDeserializeWith(Doc::class)

/**
 * Deserializes the list of [Document] to a list of objects with the specified [Doc] type
 * using the default Mongo codec registry.
 */
fun <Doc : Any> List<Document>.mongoDeserializeWith(cls: KClass<Doc>): List<Doc> {
    val codec = MongoClientSettings.getDefaultCodecRegistry().get(cls.java)
    return this.map { document ->
        codec.decode(BsonDocumentReader(document.toBsonDocument()), DecoderContext.builder().build())
    }
}

/**
 * Deserializes the [Document] to an object with the specified [Doc] type
 * using the default Mongo codec registry.
 */
inline fun <reified Doc : Any> Document.mongoDeserializeWith(): Doc = mongoDeserializeWith(Doc::class)

/**
 * Deserializes the [Document] to an object with the specified [Doc] type
 * using the default Mongo codec registry.
 */
fun <Doc : Any> Document.mongoDeserializeWith(cls: KClass<Doc>): Doc {
    val codec = MongoClientSettings.getDefaultCodecRegistry().get(cls.java)
    return codec.decode(BsonDocumentReader(this.toBsonDocument()), DecoderContext.builder().build())
}
