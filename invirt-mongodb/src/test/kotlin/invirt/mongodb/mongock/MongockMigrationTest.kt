package invirt.mongodb.mongock

import invirt.mongo.test.shouldHaveAscIndex
import invirt.mongo.test.shouldHaveDescIndex
import invirt.mongo.test.shouldHaveTimestampedIndices
import invirt.mongo.test.shouldNotHaveAscIndex
import invirt.mongo.test.testMongo
import invirt.mongodb.Mongo
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.asc
import invirt.mongodb.createIndices
import invirt.mongodb.mongock.migrations.Company
import invirt.mongodb.mongock.migrations.ordered.A_RunsSecond
import invirt.mongodb.mongock.migrations.ordered.B_RunsFirst
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mongock.api.annotations.BeforeExecution
import io.mongock.api.annotations.ChangeUnit
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
            collection.shouldHaveTimestampedIndices()
        }

        "model migration rollback" {
            val mongo = testMongo()
            shouldThrow<MongockException> {
                mongo.runMigrations("invirt.mongodb.mongock.migrations.modelrollback")
            }
            val collection = mongo.database.getCollection<Company>(Company.COLLECTION)
            collection.shouldNotHaveAscIndex("name")
        }

        "single class migration" {
            val mongo = testMongo()
            mongo.runMigration(SingleClassMigration::class)
            val collection = mongo.database.getCollection<Company>(Company.COLLECTION)
            collection.shouldHaveAscIndex("name")
            collection.shouldHaveTimestampedIndices()
        }

        "migrations run from a cached scan" {
            val scanner = ChangeUnitScanner()
            testMongo().runMigrations(DATA_PACKAGE, emptyList(), scanner::changeUnits).discovery shouldBe ChangeUnitDiscovery.SCANNED

            val mongo = testMongo()
            mongo.runMigrations(DATA_PACKAGE, emptyList(), scanner::changeUnits).discovery shouldBe ChangeUnitDiscovery.CACHED
            mongo.companyNames() shouldBe setOf("test-data-migration")
        }

        "change units run in their order, whatever order discovery lists them in" {
            val mongo = testMongo()
            val changeUnits = mongo.runMigrations(ORDERED_PACKAGE, emptyList(), ChangeUnitScanner()::changeUnits)

            changeUnits.classes shouldBe listOf(A_RunsSecond::class.java, B_RunsFirst::class.java)
            mongo.companyNames() shouldBe setOf("first", "second")
        }

        "explicit change units run what Mongock's own package scan runs, in the same order" {
            val packageScanned = testMongo()
            packageScanned.runMigrations(ORDERED_PACKAGE, emptyList()) { ChangeUnits(emptyList(), ChangeUnitDiscovery.PACKAGE_SCAN) }
            val explicit = testMongo()
            explicit.runMigrations(ORDERED_PACKAGE, emptyList(), ChangeUnitScanner()::changeUnits).discovery shouldBe
                ChangeUnitDiscovery.SCANNED

            explicit.mongockChangeLog() shouldBe packageScanned.mongockChangeLog()
            explicit.mongockChangeLog().filter { it.first.startsWith("ordered-") } shouldBe listOf(
                Triple("ordered-runs-first", "EXECUTION", "EXECUTED"),
                Triple("ordered-runs-second", "EXECUTION", "EXECUTED")
            )
        }

        "a package without change units runs through Mongock's own package scan" {
            val mongo = testMongo()
            mongo.runMigrations(PACKAGE_WITHOUT_CHANGE_UNITS, emptyList(), ChangeUnitScanner()::changeUnits) shouldBe
                ChangeUnits(emptyList(), ChangeUnitDiscovery.PACKAGE_SCAN)
            mongo.runMigrations(PACKAGE_WITHOUT_CHANGE_UNITS)
            mongo.companyNames() shouldBe emptySet()
        }

        "change units the context classloader cannot resolve run through Mongock's own package scan, on every run" {
            val scanner = ChangeUnitScanner()
            repeat(2) {
                val mongo = testMongo()
                val changeUnits = withContextClassLoader(ClassLoader.getPlatformClassLoader()) {
                    mongo.runMigrations(DATA_PACKAGE, emptyList(), scanner::changeUnits)
                }

                mongo.companyNames() shouldBe setOf("test-data-migration")
                changeUnits.discovery shouldBe ChangeUnitDiscovery.PACKAGE_SCAN
            }
        }

        "the public runMigrations reuses one scan across calls" {
            testMongo().runMigrations(DATA_PACKAGE)

            changeUnitScanner.changeUnits(DATA_PACKAGE).discovery shouldBe ChangeUnitDiscovery.CACHED
        }
    }
}

@ChangeUnit(id = "1-create-index", order = "1")
class SingleClassMigration : ModelMigration {

    @BeforeExecution
    override fun model(mongo: Mongo) {
        mongo.database.getCollection<Company>(Company.COLLECTION).createIndices(
            Company::name.asc(),
            *TimestampedDocument.allIndices()
        )
    }
}
