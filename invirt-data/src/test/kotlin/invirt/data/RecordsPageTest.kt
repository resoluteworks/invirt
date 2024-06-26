package invirt.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RecordsPageTest : StringSpec({

    "empty" {
        val recordsPage = RecordsPage.empty<String>()
        recordsPage.records shouldBe emptyList()
        recordsPage.page shouldBe Page(0, 1)
        recordsPage.count shouldBe 0
        recordsPage.sort shouldBe emptyList()
    }

    "map" {
        RecordsPage(
            records = listOf("one", "two", "three", "four"),
            count = 12432,
            page = Page(0, 10),
            sort = listOf(Sort.asc("name"))
        ).map { it.length } shouldBe RecordsPage(
            records = listOf(3, 3, 5, 4),
            count = 12432,
            page = Page(0, 10),
            sort = listOf(Sort.asc("name"))
        )
    }
})
