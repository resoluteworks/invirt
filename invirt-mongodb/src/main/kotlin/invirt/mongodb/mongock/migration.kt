package invirt.mongodb.mongock

import com.mongodb.kotlin.client.MongoClient
import invirt.mongodb.JavaClientSession
import invirt.mongodb.Mongo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackBeforeExecution
import io.mongock.api.annotations.RollbackExecution
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver
import io.mongock.runner.standalone.MongockStandalone
import io.mongock.runner.standalone.RunnerStandaloneBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger {}

internal val changeUnitScanner = ChangeUnitScanner()

/**
 * Runs migrations for a MongoDB database using the Mongock library.
 *
 * The change units of a package are found by a classpath scan that later runs of the same package reuse
 * ([ChangeUnitScanner]), and handed to Mongock as explicit classes. Mongock still orders them by each `@ChangeUnit`'s
 * `order`, and injects, executes and records them as it does for a package it scans itself.
 *
 * @param packageName The package to scan for migrations.
 * @param dependencies Optional dependencies to inject into the migrations.
 */
fun Mongo.runMigrations(
    packageName: String,
    vararg dependencies: Any
) {
    runMigrations(packageName, dependencies.toList(), changeUnitScanner::changeUnits)
}

/**
 * Runs the migrations of [packageName] with the change units [discover] finds for it, and returns them.
 */
internal fun Mongo.runMigrations(
    packageName: String,
    dependencies: List<Any>,
    discover: (String) -> ChangeUnits
): ChangeUnits {
    val (changeUnits, discoveryDuration) = measureTimedValue { discover(packageName) }
    val logPayload = mapOf(
        "packageName" to packageName,
        "changeUnits" to changeUnits.classes.size,
        "discovery" to changeUnits.discovery.logValue,
        "discoveryMs" to discoveryDuration.inWholeMilliseconds
    )
    runMigrations(dependencies, logPayload) {
        when (changeUnits.discovery) {
            ChangeUnitDiscovery.SCANNED, ChangeUnitDiscovery.CACHED -> addMigrationClasses(changeUnits.classes)
            ChangeUnitDiscovery.PACKAGE_SCAN -> addMigrationScanPackage(packageName)
        }
    }
    return changeUnits
}

/**
 * Runs a specific migration class for a MongoDB database using the Mongock library.
 *
 * @param migrationClass The migration class to run.
 * @param dependencies Optional dependencies to inject into the migration.
 */
fun Mongo.runMigration(
    migrationClass: KClass<*>,
    vararg dependencies: Any
) {
    runMigrations(dependencies.toList(), mapOf("migrationClass" to migrationClass.java.name)) {
        addMigrationClass(migrationClass.java)
    }
}

private fun Mongo.runMigrations(
    dependencies: List<Any>,
    logPayload: Map<String, Any>,
    createBuilder: RunnerStandaloneBuilder.() -> RunnerStandaloneBuilder
) {
    val durationMs = measureTimeMillis {
        // We use the same underlying Java MongoClient as the Kotlin one
        // in order for transactions to work properly across both libraries.
        val wrappedField = MongoClient::class.declaredMemberProperties.find { it.name == "wrapped" }!!
        wrappedField.isAccessible = true
        val jMongoClient = wrappedField.get(mongoClient) as com.mongodb.client.MongoClient

        val builder = MongockStandalone.builder()
            .setDriver(MongoSync4Driver.withDefaultLock(jMongoClient, databaseName))
            .setTransactional(true)
            .createBuilder()

        builder.addDependency(this)
        dependencies.forEach { builder.addDependency(it) }
        builder.buildRunner().execute()
    }
    log.atInfo {
        message = "Ran MongoDB migrations"
        payload = logPayload + ("durationMs" to durationMs)
    }
}

/**
 * Migration that only operates on data definitions for a Mongo database and which
 * aren't typically allowed to run in a transaction.
 */
interface ModelMigration {

    fun model(mongo: Mongo)

    @RollbackBeforeExecution
    fun rollbackModel(mongo: Mongo) {
    }

    // These are only required because otherwise this migration won't run simply with @BeforeExecution
    @Execution
    fun execute() {
    }

    @RollbackExecution
    fun rollback() {
    }
}

/**
 * Migration that applies data changes which can be run in a transaction
 */
interface DataMigration {

    fun data(mongo: Mongo, javaSession: JavaClientSession)

    fun rollbackData(mongo: Mongo, javaSession: JavaClientSession)
}

/**
 * Migration that applies both model and data changes.
 */
interface ModelAndDataMigration {
    fun model(mongo: Mongo)
    fun rollbackModel(mongo: Mongo)
    fun data(mongo: Mongo, javaSession: JavaClientSession)
    fun rollbackData(mongo: Mongo, javaSession: JavaClientSession)
}
