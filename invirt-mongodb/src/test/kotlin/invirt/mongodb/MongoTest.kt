package invirt.mongodb

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MongoTest : StringSpec() {

    init {
        "databaseName" {
            Mongo("mongodb://test:test@localhost:27017/local").databaseName shouldBe "local"
            Mongo("mongodb://test@localhost:27017/local?authSource=admin").databaseName shouldBe "local"
            Mongo("mongodb+srv://user:password@server.host.name.net/database1?retryWrites=true&w=majority")
                .databaseName shouldBe "database1"

            Mongo("mongodb+srv://server.host.name.net/database2?retryWrites=true&w=majority")
                .databaseName shouldBe "database2"

            shouldThrow<IllegalArgumentException> {
                Mongo("mongodb+srv://server.host.name.net/?retryWrites=true&w=majority")
            }
            shouldThrow<IllegalArgumentException> {
                Mongo("mongodb+srv://server.host.name.net?retryWrites=true&w=majority")
            }
            shouldThrow<IllegalArgumentException> {
                Mongo("mongodb://test:test@localhost:27017")
            }
            shouldThrow<IllegalArgumentException> {
                Mongo("mongodb://test:test@localhost:27017/")
            }
        }
    }
}
