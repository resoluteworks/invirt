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

    "fractional amount to string rounded" {
        Currency.getInstance("GBP").minorUnitToStringRounded(7500) shouldBe "£75"
        Currency.getInstance("GBP").minorUnitToStringRounded(0) shouldBe "£0"
        Currency.getInstance("USD").minorUnitToStringRounded(7549) shouldBe "$75"
        Currency.getInstance("USD").minorUnitToStringRounded(7550) shouldBe "$76"
        Currency.getInstance("USD").minorUnitToStringRounded(134) shouldBe "$1"
        Currency.getInstance("EUR").minorUnitToStringRounded(-32095812885) shouldBe "-€320,958,129"
        Currency.getInstance("EUR").minorUnitToStringRounded(-7549) shouldBe "-€75"
        Currency.getInstance("NGN").minorUnitToStringRounded(9023853) shouldBe "₦90,239"
        Currency.getInstance("IQD").minorUnitToStringRounded(56985) shouldBe "IQD57"
        Currency.getInstance("IQD").minorUnitToStringRounded(56400) shouldBe "IQD56"
        Currency.getInstance("ISK").minorUnitToStringRounded(1234) shouldBe "kr1,234"
        Currency.getInstance("ISK").minorUnitToStringRounded(-1234) shouldBe "-kr1,234"
    }
})
