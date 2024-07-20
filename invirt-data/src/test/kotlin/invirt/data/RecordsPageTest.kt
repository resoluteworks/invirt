package invirt.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RecordsPageTest : StringSpec({

    "empty" {
        val recordsPage = RecordsPage.empty<String>()
        recordsPage.records shouldBe emptyList()
        recordsPage.page shouldBe Page(0, 1)
        recordsPage.totalCount shouldBe 0
    }

    "map" {
        RecordsPage(
            records = listOf("one", "two", "three", "four"),
            totalCount = 12432,
            page = Page(0, 10)
        ).map { it.length } shouldBe RecordsPage(
            records = listOf(3, 3, 5, 4),
            totalCount = 12432,
            page = Page(0, 10)
        )
    }
})
