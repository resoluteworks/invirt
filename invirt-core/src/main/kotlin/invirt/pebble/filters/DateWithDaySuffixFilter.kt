package invirt.pebble.filters

import invirt.utils.formatWithDaySuffix
import io.pebbletemplates.pebble.error.PebbleException
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Formats a `LocalDate`, `LocalDateTime` or `Instant` with a `DateTimeFormatter` pattern, with the day of
 * month carrying its English ordinal suffix:
 *
 * ```
 * {{ model.deliveryDate | dateWithDaySuffix("d MMMM yyyy") }}
 * {{ model.dispatchedAt | dateWithDaySuffix("d MMMM yyyy", zone="Europe/London") }}
 * ```
 *
 * The `zone` argument is a zone id and is required for an `Instant`: an instant near midnight falls on a
 * different calendar day in different zones, so the call site is the one that has to say which zone the
 * date is read in. A `LocalDate` and a `LocalDateTime` are calendar values already, so `zone` means
 * nothing to them and is ignored if passed.
 */
class DateWithDaySuffixFilter : Filter {

    override fun getArgumentNames(): List<String> = listOf("format", "zone")

    override fun apply(
        input: Any?,
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        fun fail(message: String, cause: Throwable? = null): Nothing =
            throw PebbleException(cause, "Filter [dateWithDaySuffix] $message", lineNumber, self.name)

        val format = args["format"] as? String
            ?: fail("needs a format pattern, e.g. dateWithDaySuffix(\"d MMMM yyyy\")")
        return when (input) {
            is LocalDate -> input.formatWithDaySuffix(format)

            is LocalDateTime -> input.formatWithDaySuffix(format)

            is Instant -> {
                val zone = args["zone"] ?: fail(
                    "needs an explicit zone to format an Instant, e.g. dateWithDaySuffix(\"$format\", zone=\"Europe/London\")"
                )
                val zoneId = try {
                    ZoneId.of(zone.toString())
                } catch (e: DateTimeException) {
                    fail("was given an invalid zone id [$zone]", e)
                }
                input.formatWithDaySuffix(format, zoneId)
            }

            null -> fail("was given a null value, expected a LocalDate, LocalDateTime or Instant")

            else -> fail("can't format a value of type [${input::class.java.name}], expected a LocalDate, LocalDateTime or Instant")
        }
    }
}
