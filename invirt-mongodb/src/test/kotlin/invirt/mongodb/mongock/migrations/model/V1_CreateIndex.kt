package invirt.mongodb.mongock.migrations.model

import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.Indexes
import invirt.mongodb.Mongo
import invirt.mongodb.TimestampedDocument
import invirt.mongodb.mongock.ModelMigration
import invirt.mongodb.mongock.migrations.Company
import io.mongock.api.annotations.BeforeExecution
import io.mongock.api.annotations.ChangeUnit

@Suppress("ktlint:standard:class-naming")
@ChangeUnit(id = "1-create-index", order = "1")
class V1_CreateIndex : ModelMigration {

    @BeforeExecution
    override fun model(mongo: Mongo) {
        val companyIndices = listOf(
            IndexModel(Indexes.ascending(Company::name.name)),
            *TimestampedDocument.allIndices()
        )
        mongo.database.getCollection<Company>(Company.COLLECTION).createIndexes(companyIndices)
    }
}
