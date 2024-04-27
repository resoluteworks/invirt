package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class CurrenciesTest : StringSpec({

    "symbols" {
        Currencies.getSymbol("USD") shouldBe "$"
        Currencies.getSymbol("GBP") shouldBe "£"
        Currencies.getSymbol("EUR") shouldBe "€"
        Currencies.getSymbol("NGN") shouldBe "₦"
        Currencies.getSymbol("ALL") shouldBe "Lek"
        Currencies.getSymbol("XYZ") shouldBe "XYZ"
    }

    "fractional amount to string" {
        Currency.getInstance("GBP").minorUnitToString(134) shouldBe "£1.34"
        Currency.getInstance("GBP").minorUnitToString(0) shouldBe "£0.00"
        Currency.getInstance("USD").minorUnitToString(2385) shouldBe "$23.85"
        Currency.getInstance("EUR").minorUnitToString(-32095812885) shouldBe "-€320,958,128.85"
        Currency.getInstance("NGN").minorUnitToString(9023853) shouldBe "₦90,238.53"
        Currency.getInstance("IQD").minorUnitToString(56985) shouldBe "IQD56.985"
        Currency.getInstance("ISK").minorUnitToString(1234) shouldBe "kr1,234"
    }
})
