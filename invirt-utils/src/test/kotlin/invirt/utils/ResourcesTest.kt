package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class ResourcesTest : StringSpec({

    "resourceAsString" {
        resourceAsString("resources-test/text-file.txt").trim() shouldBe "This is the content"
    }

    "resourceAsStrings" {
        resourceAsStrings("resources-test/lines.txt") shouldBe listOf(
            "Line 1",
            "Line 2",
            "Line 3"
        )
    }

    "resourceAsProps" {
        val props = Properties()
        props["name"] = "John Smith"
        props["age"] = "20"
        resourceAsProps("resources-test/file.properties") shouldBe props
    }
})
