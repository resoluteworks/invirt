package invirt.mongodb.mongock.migrations.modelrollback

import invirt.mongodb.JavaClientSession
import invirt.mongodb.JavaMongoDatabase
import invirt.mongodb.Mongo
import invirt.mongodb.createIndices
import invirt.mongodb.mongock.ModelAndDataMigration
import invirt.mongodb.mongock.migrations.Company
import io.mongock.api.annotations.BeforeExecution
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackBeforeExecution
import io.mongock.api.annotations.RollbackExecution

@Suppress("ktlint:standard:class-naming")
@ChangeUnit(id = "1-create-index-rollback", order = "1")
class V1_CreateIndexWithRollback : ModelAndDataMigration {

    @BeforeExecution
    override fun model(mongo: Mongo) {
        mongo.database.getCollection<Company>(Company.COLLECTION).createIndices {
            asc(Company::name)
            timestampedIndices()
        }
    }

    @RollbackBeforeExecution
    override fun rollbackModel(mongo: Mongo) {
        mongo.database.getCollection<Company>(Company.COLLECTION).dropIndex("name_1")
    }

    @Execution
    override fun data(
        database: JavaMongoDatabase,
        session: JavaClientSession
    ): Unit = throw RuntimeException("Failing artificially to trigger rollback")

    @RollbackExecution
    override fun rollbackData(database: JavaMongoDatabase, session: JavaClientSession) {
    }
}
