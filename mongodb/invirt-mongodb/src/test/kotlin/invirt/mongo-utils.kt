package invirt

import com.mongodb.kotlin.client.MongoCollection
import invirt.mongodb.Mongo
import invirt.utils.uuid7
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.extensions.testcontainers.ContainerLifecycleMode
import org.testcontainers.containers.MongoDBContainer

private val log = KotlinLogging.logger {}

fun Spec.testMongo(): Mongo {
    val container = install(
        ContainerExtension(
            container = MongoDBContainer("mongo:7.0.11"),
            mode = ContainerLifecycleMode.Spec
        )
    )
    val connectionString = container.connectionString + "/${uuid7()}"
    log.info { "Test MongoDB connection string is $connectionString" }
    return Mongo(connectionString)
}

inline fun <reified Doc : Any> Mongo.randomTestCollection(): MongoCollection<Doc> = database.getCollection<Doc>(uuid7())
