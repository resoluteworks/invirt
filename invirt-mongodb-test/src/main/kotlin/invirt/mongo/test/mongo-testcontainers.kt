package invirt.mongo.test

import invirt.mongodb.Mongo
import invirt.utils.uuid7
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.TestContainerProjectExtension
import org.testcontainers.mongodb.MongoDBAtlasLocalContainer
import org.testcontainers.mongodb.MongoDBContainer

private val log = KotlinLogging.logger {}

private val mongoExtension = TestContainerProjectExtension(
    container = MongoDBContainer("mongo:8.0.17")
)

private val mongoAtlasExtension = TestContainerProjectExtension(
    container = MongoDBAtlasLocalContainer("mongodb/mongodb-atlas-local:8.0.17")
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
