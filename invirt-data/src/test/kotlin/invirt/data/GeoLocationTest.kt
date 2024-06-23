package invirt.data

import invirt.data.geo.GeoLocation
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GeoLocationTest : StringSpec({

    "normalises" {
        GeoLocation.normalised(0.53, 51.30) shouldBe GeoLocation(0.53, 51.30)
        GeoLocation.normalised(-150.4, -78.30) shouldBe GeoLocation(-150.4, -78.30)
        GeoLocation.normalised(40.4, 87.25) shouldBe GeoLocation(40.4, 87.25)

        GeoLocation.normalised(-190.2, 87.25) shouldBe GeoLocation(-180.0, 87.25)
        GeoLocation.normalised(190.2, 87.25) shouldBe GeoLocation(180.0, 87.25)

        GeoLocation.normalised(35.6, -91.2) shouldBe GeoLocation(35.6, -90.0)
        GeoLocation.normalised(35.6, 91.2) shouldBe GeoLocation(35.6, 90.0)

        GeoLocation.normalised(-190.2, 91.2) shouldBe GeoLocation(-180.0, 90.0)
        GeoLocation.normalised(190.2, 91.2) shouldBe GeoLocation(180.0, 90.0)
    }
})
