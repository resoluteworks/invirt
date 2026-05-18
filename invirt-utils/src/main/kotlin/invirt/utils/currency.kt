package invirt.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

object Currencies {

    private val symbols = resourceAsProps("currency/currency-symbols.properties")

    fun getSymbol(code: String): String = symbols.getProperty(code, code)
}

/**
 * Converts a [minorUnitAmount] (expressed in the currency's minor units, e.g. pennies
 * or cents) to a string formatted with the currency's symbol and default fraction digits.
 *
 * For example, `Currency.getInstance("GBP").minorUnitToString(134)` returns `"£1.34"`,
 * and `Currency.getInstance("EUR").minorUnitToString(-32095812885)` returns
 * `"-€320,958,128.85"`.
 */
fun Currency.minorUnitToString(minorUnitAmount: Long): String {
    val value = BigDecimal(minorUnitAmount)
        .divide(BigDecimal(10.0.pow(this.defaultFractionDigits)))

    val decimals = if (this.defaultFractionDigits > 0) {
        ".".padEnd(this.defaultFractionDigits + 1, '0')
    } else {
        ""
    }

    val symbol = Currencies.getSymbol(this.currencyCode)

    return if (minorUnitAmount >= 0) {
        symbol + DecimalFormat("#,##0$decimals").format(value)
    } else {
        "-" + symbol + DecimalFormat("#,###$decimals").format(value.abs())
    }
}

/**
 * Converts a [minorUnitAmount] to a string using the currency's symbol, but rounds the
 * value to the nearest major unit (no fractional digits). Typically useful for displaying
 * `7500` as `$75` instead of `$75.00`. Rounding uses [RoundingMode.HALF_UP].
 */
fun Currency.minorUnitToStringRounded(minorUnitAmount: Long): String {
    val value = BigDecimal(minorUnitAmount)
        .divide(BigDecimal(10.0.pow(this.defaultFractionDigits)))
        .setScale(0, RoundingMode.HALF_UP)

    val symbol = Currencies.getSymbol(this.currencyCode)

    return if (value.signum() >= 0) {
        symbol + DecimalFormat("#,##0").format(value)
    } else {
        "-" + symbol + DecimalFormat("#,###").format(value.abs())
    }
}
