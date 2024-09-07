package invirt.mongodb.mongock

import invirt.mongo.test.shouldHaveAscIndex
import invirt.mongo.test.shouldHaveDescIndex
import invirt.mongo.test.shouldNotHaveAscIndex
import invirt.mongodb.mongock.migrations.Company
import invirt.testMongo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mongock.api.exception.MongockException

class MongockMigrationTest : StringSpec() {

    init {
        "data migration" {
            val mongo = testMongo()
            mongo.runMigrations("invirt.mongodb.mongock.migrations.data")
            val collection = mongo.database.getCollection<Company>(Company.COLLECTION)
            collection.countDocuments() shouldBe 1
            collection.find().toList().first().name shouldBe "test-data-migration"
        }

        "data migration rollback" {
            val mongo = testMongo()
            shouldThrow<MongockException> {
                mongo.runMigrations("invirt.mongodb.mongock.migrations.datarollback")
            }
            val collection = mongo.database.getCollection<Company>(Company.COLLECTION)
            collection.countDocuments() shouldBe 0L
        }

        "model migration" {
            val mongo = testMongo()
            mongo.runMigrations("invirt.mongodb.mongock.migrations.model")
            val collection = mongo.database.getCollection<Company>(Company.COLLECTION)
            collection.shouldHaveAscIndex("name")
            collection.shouldHaveAscIndex("version")
            collection.shouldHaveDescIndex("createdAt")
            collection.shouldHaveDescIndex("createdAt")
        }

        "model migration rollback" {
            val mongo = testMongo()
            shouldThrow<MongockException> {
                mongo.runMigrations("invirt.mongodb.mongock.migrations.modelrollback")
            }
            val collection = mongo.database.getCollection<Company>(Company.COLLECTION)
            collection.shouldNotHaveAscIndex("name")
        }
    }
}
