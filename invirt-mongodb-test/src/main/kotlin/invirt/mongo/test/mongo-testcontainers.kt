package invirt.mongo.test

import invirt.mongodb.Mongo
import invirt.utils.uuid7
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.extensions.testcontainers.ContainerLifecycleMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait

private val log = KotlinLogging.logger {}

private val mongoExtension = ContainerExtension(
    container = MongoDBContainer("mongo:7.0.14"),
    mode = ContainerLifecycleMode.Project
)

private val mongoAtlasExtension = ContainerExtension(
    container = GenericContainer("mongodb/mongodb-atlas-local:7.0.14")
        .withExposedPorts(27017, 27027)
        .waitingFor(Wait.forListeningPorts(27017, 27027)),
    mode = ContainerLifecycleMode.Project
)

fun Spec.testMongo(): Mongo {
    val container = install(mongoExtension)
    val connectionString = container.connectionString + "/${uuid7()}"
    log.info { "Test Mongo connection string is $connectionString" }
    val mongo = Mongo(connectionString)
    afterSpec {
        mongo.close()
    }
    return mongo
}

fun Spec.testMongoAtlas(): Mongo {
    val container = install(mongoAtlasExtension)
    val connectionString = "mongodb://localhost:${container.getMappedPort(27017)}/${uuid7()}"
    log.info { "Test Mongo connection string is $connectionString" }
    val mongo = Mongo(connectionString)
    afterSpec {
        mongo.close()
    }
    return mongo
}
