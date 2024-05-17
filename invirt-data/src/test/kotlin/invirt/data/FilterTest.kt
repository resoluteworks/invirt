package invirt.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class FilterTest : StringSpec({

    "util functions" {
        Filter.eq("type", "company") shouldBe Filter("type", Filter.Operation.EQ, "company")
        Filter.gt("size", 5) shouldBe Filter("size", Filter.Operation.GT, 5)
        Filter.gte("length", 391L) shouldBe Filter("length", Filter.Operation.GTE, 391L)
        Filter.lt("dob", LocalDate.of(2024, 6, 24)) shouldBe Filter("dob", Filter.Operation.LT, LocalDate.of(2024, 6, 24))
        Filter.lte("dob", 20.5) shouldBe Filter("dob", Filter.Operation.LTE, 20.5)
        Filter.ne("status", "open") shouldBe Filter("status", Filter.Operation.NE, "open")
    }

    "from operation and value string" {
        Filter.of("type", "eq:company") shouldBe Filter("type", Filter.Operation.EQ, "company")
        Filter.of("type", "company") shouldBe Filter("type", Filter.Operation.EQ, "company")
        Filter.of("size", "gt:5") shouldBe Filter("size", Filter.Operation.GT, "5")
        Filter.of("size", "gte:24") shouldBe Filter("size", Filter.Operation.GTE, "24")
        Filter.of("size", "lt:12") shouldBe Filter("size", Filter.Operation.LT, "12")
        Filter.of("size", "lte:514") shouldBe Filter("size", Filter.Operation.LTE, "514")
        Filter.of("status", "ne:open") shouldBe Filter("status", Filter.Operation.NE, "open")

        shouldThrow<IllegalArgumentException> {
            Filter.of("status", "neq:open")
            Filter.of("status", "stop:open:123")
        }
    }
})
