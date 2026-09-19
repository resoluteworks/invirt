package invirt.pebble.functions

import invirt.utils.formatDateRange
import io.pebbletemplates.pebble.error.PebbleException
import java.time.LocalDate

/**
 * Renders a pair of dates as one phrase saying each part once, via [formatDateRange]:
 *
 * ```
 * {{ dateRange(model.openFrom, model.openTo) }}
 * ```
 *
 * A null `to` is not an error - it is how a single-day entry is modelled - but a value that is not a
 * `LocalDate` is a template bug, so it fails rather than rendering something plausible. The phrasing is
 * [formatDateRange]'s default; a template that needs another pattern formats the range in Kotlin and
 * passes the string in its model.
 */
val dateRangeFunction = pebbleFunction("dateRange", "from", "to") {
    fun fail(message: String): Nothing =
        throw PebbleException(null, "Function [dateRange] $message", lineNumber, template.name)

    val from = args["from"] as? LocalDate ?: fail("needs a LocalDate [from], was [${args["from"]}]")
    val to = args["to"]
    if (to != null && to !is LocalDate) {
        fail("needs a LocalDate or null [to], was [$to]")
    }
    formatDateRange(from, to as LocalDate?)
}
