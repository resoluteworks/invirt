package invirt.mongodb

import com.mongodb.client.model.Sorts
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SortTest : StringSpec({
    "KProperty mongoSortAsc/Desc" {
        data class TestDocument(
            val index: Int,
            val name: String
        )
        TestDocument::index.mongoSortAsc() shouldBe Sorts.ascending("index")
        TestDocument::name.mongoSortDesc() shouldBe Sorts.descending("name")
    }
})
