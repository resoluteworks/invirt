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
})
