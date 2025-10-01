package invirt.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

object Currencies {

    private val symbols = resourceAsProps("currency/currency-symbols.properties")

    fun getSymbol(code: String): String = symbols.getProperty(code, code)
}

fun Currency.minorUnitToString(decimalAmount: Long): String {
    val value = BigDecimal(decimalAmount)
        .divide(BigDecimal(10.0.pow(this.defaultFractionDigits)))

    val decimals = if (this.defaultFractionDigits > 0) {
        ".".padEnd(this.defaultFractionDigits + 1, '0')
    } else {
        ""
    }

    val symbol = Currencies.getSymbol(this.currencyCode)

    return if (decimalAmount >= 0) {
        symbol + DecimalFormat("#,##0$decimals").format(value)
    } else {
        "-" + symbol + DecimalFormat("#,###$decimals").format(value.abs())
    }
}
