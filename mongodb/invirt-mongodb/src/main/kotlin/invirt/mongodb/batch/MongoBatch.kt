package invirt.mongodb.batch

import com.mongodb.kotlin.client.MongoCollection

/**
 * A batch of documents to be inserted into a MongoDB collection.
 */
class MongoBatch<Doc : Any>(
    private val collection: MongoCollection<Doc>,
    private val size: Int = 1000
) : AutoCloseable {

    private val documents = mutableListOf<Doc>()

    fun add(document: Doc) {
        documents.add(document)
        if (documents.size == size) {
            bulkIndex()
        }
    }

    fun addAll(documents: Collection<Doc>) {
        documents.forEach { add(it) }
    }

    override fun close() {
        if (documents.size > 0) {
            bulkIndex()
        }
    }

    private fun bulkIndex() {
        collection.insertMany(documents)
        documents.clear()
    }
}

/**
 * Creates a new [MongoBatch] for the given [MongoCollection] and executes the [block] function.
 * The batch will be automatically closed after the block is executed.
 */
fun <Doc : Any> MongoCollection<Doc>.withBatch(size: Int = 1000, block: (MongoBatch<Doc>) -> Unit) {
    MongoBatch(this, size).use(block)
}
