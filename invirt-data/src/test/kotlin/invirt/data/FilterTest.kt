package invirt.data

import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import io.kotest.assertions.throwables.shouldThrowWithMessage
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

    "CompoundFilter empty children" {
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            CompoundFilter(CompoundFilter.Operator.OR, emptySet())
        }
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            CompoundFilter(CompoundFilter.Operator.AND, emptySet())
        }
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            CompoundFilter.or(emptySet())
        }
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            CompoundFilter.and(emptySet())
        }
    }

    "Filter.flatten" {
        CompoundFilter.or("status".eq("open"), "size".gt(10)).flatten() shouldBe CompoundFilter.or("status".eq("open"), "size".gt(10))

        CompoundFilter.or("status".eq("open")).flatten() shouldBe "status".eq("open")

        CompoundFilter.and(CompoundFilter.or("status".eq("open"))).flatten() shouldBe "status".eq("open")

        CompoundFilter.and(
            CompoundFilter.or("status".eq("open"), "size".gt(10)),
            "active".eq(true)
        ).flatten() shouldBe CompoundFilter.and(
            CompoundFilter.or("status".eq("open"), "size".gt(10)),
            "active".eq(true)
        )

        CompoundFilter.and(
            CompoundFilter.or("status".eq("open"), "size".gt(10)),
            CompoundFilter.or("enabled".eq(false)),
            "active".eq(true)
        ).flatten() shouldBe CompoundFilter.and(
            CompoundFilter.or("status".eq("open"), "size".gt(10)),
            "enabled".eq(false),
            "active".eq(true)
        )
    }

    "Collection.orFilter()" {
        emptySet<Filter>().orFilter() shouldBe null

        listOf("type".eq("person"), "size".gte(10)).orFilter() shouldBe
            CompoundFilter(CompoundFilter.Operator.OR, listOf("type".eq("person"), "size".gte(10)))

        listOf("type".eq("person")).orFilter() shouldBe CompoundFilter.or("type".eq("person"))
    }

    "Collection.andFilter()" {
        emptySet<Filter>().andFilter() shouldBe null

        listOf("type".eq("person"), "size".gte(10)).andFilter() shouldBe
            CompoundFilter(CompoundFilter.Operator.AND, listOf("type".eq("person"), "size".gte(10)))

        listOf("type".eq("person")).andFilter() shouldBe CompoundFilter.and("type".eq("person"))
    }

    "FieldFilter.map" {
        ("size" eq "10").map { it.toInt() } shouldBe ("size" eq 10)
    }

    "KProperty" {
        data class Pojo(
            val title: String,
            val size: Int,
            val status: Status?
        )
        Pojo::title.eq("Work in progress") shouldBe FieldFilter.eq("title", "Work in progress")
        Pojo::status.ne(Status.PUBLISHED) shouldBe FieldFilter.ne("status", Status.PUBLISHED)
        Pojo::size.gt(20) shouldBe FieldFilter.gt("size", 20)
        Pojo::size.gte(5) shouldBe FieldFilter.gte("size", 5)
        Pojo::size.lt(32) shouldBe FieldFilter.lt("size", 32)
        Pojo::size.lte(45) shouldBe FieldFilter.lte("size", 45)
    }

    "String" {
        "title".eq("Work in progress") shouldBe FieldFilter.eq("title", "Work in progress")
        "status".ne(Status.PUBLISHED) shouldBe FieldFilter.ne("status", Status.PUBLISHED)
        "size".gt(20) shouldBe FieldFilter.gt("size", 20)
        "size".gte(5) shouldBe FieldFilter.gte("size", 5)
        "size".lt(32) shouldBe FieldFilter.lt("size", 32)
        "size".lte(45) shouldBe FieldFilter.lte("size", 45)
        "location".withinGeoBounds(
            GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
        ) shouldBe
            FieldFilter(
                "location",
                FieldFilter.Operation.WITHIN_GEO_BOUNDS,
                GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
            )
    }
}) {
    enum class Status {
        DRAFT,
        PUBLISHED
    }
}
