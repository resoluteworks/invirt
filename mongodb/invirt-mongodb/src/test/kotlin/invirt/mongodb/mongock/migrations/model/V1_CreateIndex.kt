package invirt.mongodb.mongock.migrations.model

import invirt.mongodb.Mongo
import invirt.mongodb.createIndices
import invirt.mongodb.indexAsc
import invirt.mongodb.mongock.ModelMigration
import invirt.mongodb.mongock.migrations.Company
import io.mongock.api.annotations.BeforeExecution
import io.mongock.api.annotations.ChangeUnit

@Suppress("ktlint:standard:class-naming")
@ChangeUnit(id = "1-create-index", order = "1")
class V1_CreateIndex : ModelMigration {

    @BeforeExecution
    override fun model(mongo: Mongo) {
        mongo.database.getCollection<Company>(Company.COLLECTION).createIndices(
            Company::name.indexAsc()
        )
    }
}
