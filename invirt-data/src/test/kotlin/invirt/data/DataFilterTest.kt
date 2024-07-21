package invirt.data

import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DataFilterTest : StringSpec({

    "compound filter - or" {
        DataFilter.Compound.or(DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open")) shouldBe
            DataFilter.Compound(
                DataFilter.Compound.Operator.OR, listOf(DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open"))
            )

        DataFilter.Compound.or(
            listOf(
                DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open")
            )
        ) shouldBe DataFilter.Compound(
            DataFilter.Compound.Operator.OR, listOf(DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open"))
        )
    }

    "compound filter - and" {
        DataFilter.Compound.and(DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open")) shouldBe DataFilter.Compound(
            DataFilter.Compound.Operator.AND, listOf(DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open"))
        )

        DataFilter.Compound.and(
            listOf(
                DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open")
            )
        ) shouldBe DataFilter.Compound(
            DataFilter.Compound.Operator.AND, listOf(DataFilter.Field.gte("field", 10), DataFilter.Field.ne("status", "open"))
        )
    }

    "DataFilter.Compound empty children" {
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            DataFilter.Compound(DataFilter.Compound.Operator.OR, emptySet())
        }
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            DataFilter.Compound(DataFilter.Compound.Operator.AND, emptySet())
        }
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            DataFilter.Compound.or(emptySet())
        }
        shouldThrowWithMessage<IllegalArgumentException>("children argument cannot be an empty collection") {
            DataFilter.Compound.and(emptySet())
        }
    }

    "Filter.flatten" {
        DataFilter.Compound.or("status".eq("open"), "size".gt(10)).flatten() shouldBe DataFilter.Compound.or(
            "status".eq("open"), "size".gt(10)
        )

        DataFilter.Compound.or("status".eq("open")).flatten() shouldBe "status".eq("open")

        DataFilter.Compound.and(DataFilter.Compound.or("status".eq("open"))).flatten() shouldBe "status".eq("open")

        DataFilter.Compound.and(
            DataFilter.Compound.or("status".eq("open"), "size".gt(10)), "active".eq(true)
        ).flatten() shouldBe DataFilter.Compound.and(
            DataFilter.Compound.or("status".eq("open"), "size".gt(10)), "active".eq(true)
        )

        DataFilter.Compound.and(
            DataFilter.Compound.or("status".eq("open"), "size".gt(10)), DataFilter.Compound.or("enabled".eq(false)), "active".eq(true)
        ).flatten() shouldBe DataFilter.Compound.and(
            DataFilter.Compound.or("status".eq("open"), "size".gt(10)), "enabled".eq(false), "active".eq(true)
        )
    }

    "Collection.orFilter()" {
        emptySet<DataFilter>().orFilter() shouldBe null

        listOf("type".eq("person"), "size".gte(10)).orFilter() shouldBe DataFilter.Compound(
            DataFilter.Compound.Operator.OR, listOf("type".eq("person"), "size".gte(10))
        )

        listOf("type".eq("person")).orFilter() shouldBe DataFilter.Compound.or("type".eq("person"))
    }

    "Collection.andFilter()" {
        emptySet<DataFilter>().andFilter() shouldBe null

        listOf("type".eq("person"), "size".gte(10)).andFilter() shouldBe DataFilter.Compound(
            DataFilter.Compound.Operator.AND, listOf("type".eq("person"), "size".gte(10))
        )

        listOf("type".eq("person")).andFilter() shouldBe DataFilter.Compound.and("type".eq("person"))
    }

    "DataFilter.Field.map" {
        ("size" eq "10").map { it.toInt() } shouldBe ("size" eq 10)
    }

    "exist and doesntExist" {
        DataFilter.Field.exists("name.lastName") shouldBe DataFilter.Field("name.lastName", DataFilter.Field.Operation.EXISTS, Unit)
        DataFilter.Field.doesntExist("name") shouldBe DataFilter.Field("name", DataFilter.Field.Operation.DOESNT_EXIST, Unit)
    }
    "KProperty" {
        data class Document(
            val title: String,
            val size: Int,
            val status: Status?
        )
        Document::title.eq("Work in progress") shouldBe DataFilter.Field.eq("title", "Work in progress")
        Document::status.ne(Status.PUBLISHED) shouldBe DataFilter.Field.ne("status", Status.PUBLISHED)
        Document::size.gt(20) shouldBe DataFilter.Field.gt("size", 20)
        Document::size.gte(5) shouldBe DataFilter.Field.gte("size", 5)
        Document::size.lt(32) shouldBe DataFilter.Field.lt("size", 32)
        Document::size.lte(45) shouldBe DataFilter.Field.lte("size", 45)
        Document::status.exists() shouldBe DataFilter.Field.exists("status")
        Document::status.doesntExist() shouldBe DataFilter.Field.doesntExist("status")

        val filter = orFilter(
            Document::status.ne(Status.PUBLISHED),
            andFilter(
                Document::status.eq(Status.PUBLISHED),
                Document::size.gte(200000)
            )
        )
    }

    "String" {
        "title".eq("Work in progress") shouldBe DataFilter.Field.eq("title", "Work in progress")
        "status".ne(Status.PUBLISHED) shouldBe DataFilter.Field.ne("status", Status.PUBLISHED)
        "size".gt(20) shouldBe DataFilter.Field.gt("size", 20)
        "size".gte(5) shouldBe DataFilter.Field.gte("size", 5)
        "size".lt(32) shouldBe DataFilter.Field.lt("size", 32)
        "size".lte(45) shouldBe DataFilter.Field.lte("size", 45)
        "location.lng".exists() shouldBe DataFilter.Field.exists("location.lng")
        "location.lat".doesntExist() shouldBe DataFilter.Field.doesntExist("location.lat")

        "location".withinGeoBounds(
            GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
        ) shouldBe DataFilter.Field(
            "location",
            DataFilter.Field.Operation.WITHIN_GEO_BOUNDS,
            GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
        )
    }
}) {
    enum class Status {
        DRAFT,
        PUBLISHED
    }
}
