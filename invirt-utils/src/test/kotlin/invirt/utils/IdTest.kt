package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan

class IdTest : StringSpec({

    "uuid7" {
        repeat(1000) {
            uuid7() shouldBeLessThan uuid7()
        }
    }
})
