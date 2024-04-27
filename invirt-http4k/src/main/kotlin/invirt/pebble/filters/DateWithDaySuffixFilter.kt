package invirt.pebble.filters

import invirt.utils.formatWithDaySuffix
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.time.LocalDate

class DateWithDaySuffixFilter : Filter {

    override fun getArgumentNames(): List<String> = listOf("format")

    override fun apply(
        input: Any,
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int,
    ): Any {
        val format = args["format"] as String
        return (input as LocalDate).formatWithDaySuffix(format)
    }
}
