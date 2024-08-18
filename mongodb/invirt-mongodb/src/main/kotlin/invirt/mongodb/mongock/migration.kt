package invirt.mongodb.mongock

import com.mongodb.client.MongoClients
import invirt.mongodb.JavaClientSession
import invirt.mongodb.JavaMongoDatabase
import invirt.mongodb.Mongo
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackBeforeExecution
import io.mongock.api.annotations.RollbackExecution
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver
import io.mongock.runner.standalone.MongockStandalone

fun Mongo.runMigrations(packageName: String) {
    MongockStandalone.builder()
        .setDriver(MongoSync4Driver.withDefaultLock(MongoClients.create(connectionString), databaseName))
        .addMigrationScanPackage(packageName)
        .setTransactionEnabled(true)
        .addDependency(this)
        .buildRunner()
        .execute()
}

/**
 * Migration that only operates on data definitions for a Mongo database and which
 * aren't typically allowed to run in a transaction
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

    fun data(database: JavaMongoDatabase, session: JavaClientSession)

    fun rollbackData(database: JavaMongoDatabase, session: JavaClientSession)
}

interface ModelAndDataMigration {
    fun model(mongo: Mongo)
    fun rollbackModel(mongo: Mongo)
    fun data(database: JavaMongoDatabase, session: JavaClientSession)
    fun rollbackData(database: JavaMongoDatabase, session: JavaClientSession)
}
