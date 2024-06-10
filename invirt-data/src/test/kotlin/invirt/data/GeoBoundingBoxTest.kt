package invirt.data

import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GeoBoundingBoxTest : StringSpec({

    "points" {
        GeoBoundingBox(GeoLocation(lng = -8.596867, lat = 51.348611), GeoLocation(lng = 1.950008, lat = 56.435911))
            .points shouldBe listOf(
            GeoLocation(lng = -8.596867, lat = 51.348611),
            GeoLocation(lng = -8.596867, lat = 56.435911),
            GeoLocation(lng = 1.950008, lat = 56.435911),
            GeoLocation(lng = 1.950008, lat = 51.348611)
        )
    }

    "from array/list" {
        GeoBoundingBox(-8.596867, 51.348611, 1.950008, 56.435911) shouldBe GeoBoundingBox(
            GeoLocation(lng = -8.596867, lat = 51.348611),
            GeoLocation(lng = 1.950008, lat = 56.435911)
        )
    }

    "from array/list - invalid args" {
        shouldThrowWithMessage<IllegalArgumentException>("Bounding box coordinates array must be of size 4") {
            GeoBoundingBox()
        }
        shouldThrowWithMessage<IllegalArgumentException>("Bounding box coordinates array must be of size 4") {
            GeoBoundingBox(-8.596867)
        }
        shouldThrowWithMessage<IllegalArgumentException>("Bounding box coordinates array must be of size 4") {
            GeoBoundingBox(-8.596867, 51.348611, 1.950008, 56.435911, 56.435911)
        }
    }

    "from string" {
        GeoBoundingBox("-8.596867,51.348611,1.950008,56.435911") shouldBe GeoBoundingBox(
            GeoLocation(lng = -8.596867, lat = 51.348611),
            GeoLocation(lng = 1.950008, lat = 56.435911)
        )
    }
})
