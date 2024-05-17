package invirt.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FilterTest : StringSpec({

    "compound filter - or" {
        CompoundFilter.or(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open")) shouldBe CompoundFilter(
            CompoundFilter.Operator.OR,
            listOf(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open"))
        )
        CompoundFilter.or(listOf(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open"))) shouldBe CompoundFilter(
            CompoundFilter.Operator.OR,
            listOf(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open"))
        )
    }

    "compound filter - and" {
        CompoundFilter.and(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open")) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open"))
        )
        CompoundFilter.and(listOf(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open"))) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(FieldFilter.gte("field", 10), FieldFilter.ne("status", "open"))
        )
    }
})
