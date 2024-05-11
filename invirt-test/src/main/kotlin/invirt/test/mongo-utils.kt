package invirt.test

import com.mongodb.kotlin.client.MongoCollection
import com.mongodb.kotlin.client.MongoDatabase
import invirt.mongodb.*
import invirt.utils.uuid7
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.extensions.testcontainers.ContainerLifecycleMode
import org.testcontainers.containers.MongoDBContainer

private val log = KotlinLogging.logger {}
private val mongoContainerExtension = ContainerExtension(
    container = MongoDBContainer("mongo:7.0.4"),
    mode = ContainerLifecycleMode.Project
)

fun Spec.testMongo(): Mongo {
    val container = install(mongoContainerExtension)
    val connectionString = container.connectionString + "/${uuid7()}"
    log.info { "Test MongoDB connection string is $connectionString" }
    return Mongo(connectionString)
}

inline fun <reified E : StoredEntity> MongoDatabase.randomCollection(): MongoCollection<E> {
    return getCollection<E>(uuid7())
}

inline fun <reified E : StoredEntity> MongoDatabase.deleteCollection() {
    getCollection<E>(collectionName<E>()).drop()
    collection<E>() // So indices get recreated
}
