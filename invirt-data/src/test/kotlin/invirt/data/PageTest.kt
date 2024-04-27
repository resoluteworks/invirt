package invirt.data

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PageTest : StringSpec({

    "pageIndex" {
        Page(0, 10).pageIndex shouldBe 0
        Page(10, 10).pageIndex shouldBe 1
        Page(300, 100).pageIndex shouldBe 3
        shouldThrowWithMessage<IllegalArgumentException>("from must be a multiple of size or 0") {
            Page(25, 40)
        }
        shouldThrowWithMessage<IllegalArgumentException>("Page size must be greater than 0") {
            Page(10, 0)
        }
    }

    "page list" {
        (1..10).toList().page(Page(0, 10)) shouldBe (1..10).toList()
        (1..100).toList().page(Page(0, 10)) shouldBe (1..10).toList()
        (1..100).toList().page(Page(20, 10)) shouldBe (21..30).toList()
        (1..95).toList().page(Page(90, 10)) shouldBe (91..95).toList()
    }
})
