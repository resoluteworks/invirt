package invirt.mongodb.mongock.migrations.modelrollback

import com.mongodb.client.model.Indexes
import invirt.mongodb.JavaClientSession
import invirt.mongodb.Mongo
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
        mongo.database.getCollection<Company>(Company.COLLECTION).createIndex(Indexes.ascending(Company::name.name))
    }

    @RollbackBeforeExecution
    override fun rollbackModel(mongo: Mongo) {
        mongo.database.getCollection<Company>(Company.COLLECTION).dropIndex("name_1")
    }

    @Execution
    override fun data(
        mongo: Mongo,
        javaSession: JavaClientSession
    ): Unit = throw RuntimeException("Failing artificially to trigger rollback")

    @RollbackExecution
    override fun rollbackData(mongo: Mongo, javaSession: JavaClientSession) {
    }
}
