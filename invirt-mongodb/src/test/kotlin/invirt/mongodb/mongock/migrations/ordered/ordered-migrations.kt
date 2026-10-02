package invirt.mongodb.mongock.migrations.ordered

import com.mongodb.client.model.Filters
import invirt.mongodb.JavaClientSession
import invirt.mongodb.Mongo
import invirt.mongodb.kotlin
import invirt.mongodb.mongock.DataMigration
import invirt.mongodb.mongock.migrations.Company
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution

/**
 * Sorts first by name but runs second by `order`, and fails unless [B_RunsFirst] has already run.
 */
@Suppress("ktlint:standard:class-naming")
@ChangeUnit(id = "ordered-runs-second", order = "2")
class A_RunsSecond : DataMigration {

    @Execution
    override fun data(mongo: Mongo, javaSession: JavaClientSession) {
        val session = javaSession.kotlin()
        val companies = mongo.database.getCollection(Company.COLLECTION, Company::class.java)
        check(companies.countDocuments(session, Filters.eq("name", "first")) == 1L) { "ordered-runs-first has not run" }
        companies.insertOne(session, Company("second"))
    }

    @RollbackExecution
    override fun rollbackData(mongo: Mongo, javaSession: JavaClientSession) {
    }
}

/**
 * Sorts second by name but runs first by `order`.
 */
@Suppress("ktlint:standard:class-naming")
@ChangeUnit(id = "ordered-runs-first", order = "1")
class B_RunsFirst : DataMigration {

    @Execution
    override fun data(mongo: Mongo, javaSession: JavaClientSession) {
        mongo.database.getCollection(Company.COLLECTION, Company::class.java).insertOne(javaSession.kotlin(), Company("first"))
    }

    @RollbackExecution
    override fun rollbackData(mongo: Mongo, javaSession: JavaClientSession) {
    }
}
