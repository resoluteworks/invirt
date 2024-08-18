package invirt.mongodb.mongock.migrations

import invirt.mongodb.TimestampedDocument
import invirt.mongodb.mongoNow
import invirt.utils.uuid7
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

data class Company(
    val name: String,
    @BsonId override val id: String = uuid7(),
    override var version: Long = 0,
    override var createdAt: Instant = mongoNow(),
    override var updatedAt: Instant = mongoNow()
) : TimestampedDocument {

    companion object {
        const val COLLECTION = "companies"
    }
}
