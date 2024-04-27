package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EnumTest : StringSpec({

    "valueOfOrNull" {
        valueOfOrNull<ValuesTestEnum>("PRODUCT") shouldBe ValuesTestEnum.PRODUCT
        valueOfOrNull<ValuesTestEnum>("DESIGN") shouldBe ValuesTestEnum.DESIGN
        valueOfOrNull<ValuesTestEnum>("DELIVERY") shouldBe ValuesTestEnum.DELIVERY
        valueOfOrNull<ValuesTestEnum>("delivery") shouldBe null
        valueOfOrNull<ValuesTestEnum>("invalid value") shouldBe null
    }

    "toEnumValues" {
        "PRODUCT".toEnumValues<ValuesTestEnum>() shouldBe listOf(ValuesTestEnum.PRODUCT)
        "PRODUCT,DESIGN".toEnumValues<ValuesTestEnum>() shouldBe listOf(ValuesTestEnum.PRODUCT, ValuesTestEnum.DESIGN)
        " DESIGN  \t , DELIVERY".toEnumValues<ValuesTestEnum>() shouldBe listOf(
            ValuesTestEnum.DESIGN,
            ValuesTestEnum.DELIVERY
        )
        null.toEnumValues<ValuesTestEnum>() shouldBe emptyList()
        "".toEnumValues<ValuesTestEnum>() shouldBe emptyList()
        " \t".toEnumValues<ValuesTestEnum>() shouldBe emptyList()
    }
})

private enum class ValuesTestEnum {
    PRODUCT,
    DESIGN,
    DELIVERY
}
