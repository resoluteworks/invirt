package invirt.mongodb.mongock.migrations.data

import com.mongodb.client.model.Filters
import invirt.mongodb.JavaClientSession
import invirt.mongodb.Mongo
import invirt.mongodb.kotlin
import invirt.mongodb.mongock.DataMigration
import invirt.mongodb.mongock.migrations.Company
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution

@Suppress("ktlint:standard:class-naming")
@ChangeUnit(id = "1-data", order = "1")
class V1_Data : DataMigration {

    @Execution
    override fun data(mongo: Mongo, javaSession: JavaClientSession) {
        val session = javaSession.kotlin()
        mongo.database.getCollection(Company.COLLECTION, Company::class.java).insertOne(session, Company("test-data-migration"))
    }

    @RollbackExecution
    override fun rollbackData(mongo: Mongo, javaSession: JavaClientSession) {
        val session = javaSession.kotlin()
        mongo.database.getCollection(Company.COLLECTION, Company::class.java).deleteMany(session, Filters.empty())
    }
}
