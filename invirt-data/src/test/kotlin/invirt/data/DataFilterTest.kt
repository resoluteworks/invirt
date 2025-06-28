package invirt.data

import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DataFilterTest : StringSpec({

    "DataFilter.or" {
        DataFilter.or(DataFilter.gte("field", 10), DataFilter.ne("status", "open")) shouldBe
            DataFilter.Or(
                listOf(DataFilter.gte("field", 10), DataFilter.ne("status", "open"))
            )
    }

    "DataFilter.and" {
        DataFilter.and(DataFilter.gte("field", 10), DataFilter.ne("status", "open")) shouldBe DataFilter.And(
            listOf(DataFilter.gte("field", 10), DataFilter.ne("status", "open"))
        )
    }

    "DataFilter.Or and DataFilter.And should throw on empty collections" {
        shouldThrowWithMessage<IllegalArgumentException>("Filter collection cannot be empty for an OR filter") {
            DataFilter.Or(emptyList())
        }
        shouldThrowWithMessage<IllegalArgumentException>("Filter collection cannot be empty for an AND filter") {
            DataFilter.And(emptyList())
        }
    }

    "Filter.flatten" {
        DataFilter.or("status".eq("open"), "size".gt(10)).flatten() shouldBe DataFilter.or(
            "status".eq("open"), "size".gt(10)
        )

        DataFilter.or("status".eq("open")).flatten() shouldBe "status".eq("open")
        DataFilter.or("enabled".eq(false)).flatten() shouldBe "enabled".eq(false)

        DataFilter.and(DataFilter.or("status".eq("open"))).flatten() shouldBe "status".eq("open")

        DataFilter.and(
            DataFilter.or("status".eq("open"), "size".gt(10)), "active".eq(true)
        ).flatten() shouldBe DataFilter.and(
            DataFilter.or("status".eq("open"), "size".gt(10)), "active".eq(true)
        )

        DataFilter.and(
            DataFilter.or("status".eq("open"), "size".gt(10)), DataFilter.or("enabled".eq(false)), "active".eq(true)
        ).flatten() shouldBe DataFilter.and(
            DataFilter.or("status".eq("open"), "size".gt(10)), "enabled".eq(false), "active".eq(true)
        )
    }

    "exist and doesntExist" {
        DataFilter.exists("name.lastName") shouldBe DataFilter.Field.Exists("name.lastName")
        DataFilter.doesntExist("name") shouldBe DataFilter.Field.DoesntExist("name")
    }

    "KProperty" {
        data class Document(
            val title: String,
            val size: Int,
            val status: Status?
        )
        Document::title.eq("Work in progress") shouldBe DataFilter.Field.Eq("title", "Work in progress")
        Document::status.ne(Status.PUBLISHED) shouldBe DataFilter.Field.Ne("status", Status.PUBLISHED)
        Document::size.gt(20) shouldBe DataFilter.Field.Gt("size", 20)
        Document::size.gte(5) shouldBe DataFilter.Field.Gte("size", 5)
        Document::size.lt(32) shouldBe DataFilter.Field.Lt("size", 32)
        Document::size.lte(45) shouldBe DataFilter.Field.Lte("size", 45)
        Document::status.exists() shouldBe DataFilter.Field.Exists("status")
        Document::status.doesntExist() shouldBe DataFilter.Field.DoesntExist("status")
    }

    "String" {
        "title".eq("Work in progress") shouldBe DataFilter.Field.Eq("title", "Work in progress")
        "status".ne(Status.PUBLISHED) shouldBe DataFilter.Field.Ne("status", Status.PUBLISHED)
        "size".gt(20) shouldBe DataFilter.Field.Gt("size", 20)
        "size".gte(5) shouldBe DataFilter.Field.Gte("size", 5)
        "size".lt(32) shouldBe DataFilter.Field.Lt("size", 32)
        "size".lte(45) shouldBe DataFilter.Field.Lte("size", 45)
        "location.lng".exists() shouldBe DataFilter.Field.Exists("location.lng")
        "location.lat".doesntExist() shouldBe DataFilter.Field.DoesntExist("location.lat")

        "location".withinGeoBounds(
            GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
        ) shouldBe DataFilter.Field.WithinGeoBounds(
            "location",
            GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
        )
    }
}) {
    enum class Status {
        DRAFT,
        PUBLISHED
    }
}
