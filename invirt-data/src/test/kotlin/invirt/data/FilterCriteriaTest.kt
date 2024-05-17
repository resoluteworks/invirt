package invirt.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FilterCriteriaTest : StringSpec({

    "compound criteria - or" {
        CompoundCriteria.or(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open")) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.OR,
            listOf(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open"))
        )
        CompoundCriteria.or(listOf(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open"))) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.OR,
            listOf(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open"))
        )
    }

    "compound criteria - and" {
        CompoundCriteria.and(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open")) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open"))
        )
        CompoundCriteria.and(listOf(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open"))) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(FieldCriteria.gte("field", 10), FieldCriteria.ne("status", "open"))
        )
    }
})
