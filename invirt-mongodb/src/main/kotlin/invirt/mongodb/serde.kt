package invirt.mongodb

import com.mongodb.kotlin.client.MongoCollection
import org.bson.BsonDocumentReader
import org.bson.Document
import org.bson.codecs.DecoderContext

/**
 * Deserializes the specified [documents] collection to a list of objects with the
 * type of this collection's [MongoCollection.documentClass].
 */
fun <Doc : Any> MongoCollection<Doc>.deserialize(documents: List<Document>): List<Doc> {
    val codec = codecRegistry.get(documentClass)
    return documents.map { document ->
        codec.decode(BsonDocumentReader(document.toBsonDocument()), DecoderContext.builder().build())
    }
}

/**
 * Deserializes the specified [documents] collection to a list of objects with the specified [Doc] type.
 */
inline fun <reified Doc : Any> MongoCollection<*>.deserializeWith(documents: List<Document>): List<Doc> {
    val codec = codecRegistry.get(Doc::class.java)
    return documents.map { document ->
        codec.decode(BsonDocumentReader(document.toBsonDocument()), DecoderContext.builder().build())
    }
}

/**
 * Deserializes the specified [document] to an object with the specified [Doc] type.
 */
inline fun <reified Doc : Any> MongoCollection<*>.deserializeWith(document: Document): Doc =
    codecRegistry.get(Doc::class.java).decode(BsonDocumentReader(document.toBsonDocument()), DecoderContext.builder().build())
