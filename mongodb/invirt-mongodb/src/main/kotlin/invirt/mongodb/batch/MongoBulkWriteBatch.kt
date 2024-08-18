package invirt.mongodb.batch

import com.mongodb.client.model.WriteModel
import com.mongodb.kotlin.client.MongoCollection

class MongoBulkWriteBatch<Doc : Any>(
    private val collection: MongoCollection<Doc>,
    private val size: Int = 1000
) : AutoCloseable {

    private val documents = mutableListOf<WriteModel<Doc>>()

    fun add(writeModel: WriteModel<Doc>) {
        documents.add(writeModel)
        if (documents.size == size) {
            bulkIndex()
        }
    }

    fun addAll(documents: Collection<WriteModel<Doc>>) {
        documents.forEach { add(it) }
    }

    override fun close() {
        if (documents.size > 0) {
            bulkIndex()
        }
    }

    private fun bulkIndex() {
        collection.bulkWrite(documents)
        documents.clear()
    }
}

fun <Doc : Any> MongoCollection<Doc>.withBulkWriteBatch(size: Int = 1000, block: (MongoBulkWriteBatch<Doc>) -> Unit) {
    MongoBulkWriteBatch(this, size).use(block)
}
