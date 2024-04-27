package invirt.test

import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import invirt.data.mongodb.MongoEntity
import invirt.data.mongodb.TimestampedEntity
import invirt.data.mongodb.collectionName
import invirt.data.mongodb.database
import invirt.data.mongodb.getEntityCollection
import invirt.data.mongodb.mongoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.withClue
import io.kotest.core.extensions.install
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.extensions.testcontainers.ContainerLifecycleMode
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.MongoDBContainer
import java.time.temporal.ChronoUnit
import java.util.*

private val log = KotlinLogging.logger {}
private val mongoContainerExtension = ContainerExtension(
    container = MongoDBContainer("mongo:7.0.4"),
    mode = ContainerLifecycleMode.Project
)

fun Spec.testMongoClient(closeAfterSpec: Boolean = true): MongoClient {
    val container = install(mongoContainerExtension)
    log.info { "Test MongoDB connection string is ${container.connectionString}" }
    val mongoClient = mongoClient(container.connectionString)
    if (closeAfterSpec) {
        this.listener(object : TestListener {
            override suspend fun afterSpec(spec: Spec) {
                mongoClient.close()
            }
        })
    }
    return mongoClient
}

fun MongoClient.randomDatabase(): MongoDatabase {
    val dbName = "test-${UUID.randomUUID()}"
    return database(dbName)
}

fun Spec.testMongoConnectionString(): String {
    val container = install(mongoContainerExtension)
    val dbName = "test-${UUID.randomUUID()}"
    val connectionString = "${container.connectionString}/$dbName"
    return connectionString
}

inline fun <reified T : Any> MongoDatabase.testCollection(): MongoCollection<T> {
    return getCollection<T>(UUID.randomUUID().toString())
}

inline fun <reified T : MongoEntity> MongoDatabase.deleteCollection() {
    getCollection<T>(collectionName<T>()).drop()
    getEntityCollection<T>() // So indices get recreated
}

infix fun TimestampedEntity.shouldBeUpdateOf(other: TimestampedEntity) {
    val thisUpdatedAt = this.updatedAt.truncatedTo(ChronoUnit.MILLIS)
    val otherUpdatedAt = other.updatedAt.truncatedTo(ChronoUnit.MILLIS)
    withClue("$thisUpdatedAt is not after $otherUpdatedAt") {
        thisUpdatedAt.isAfter(otherUpdatedAt) shouldBe true
    }
}
