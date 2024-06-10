package invirt.data

import invirt.data.geo.GeoBoundingBox
import invirt.data.geo.GeoLocation
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
})
