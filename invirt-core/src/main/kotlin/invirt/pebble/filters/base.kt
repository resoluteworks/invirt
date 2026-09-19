package invirt.pebble.filters

import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate

abstract class PebbleFilter(val name: String) : Filter

/**
 * What a [pebbleFilter] body is given about the call it is answering. [name] and [lineNumber] are what a
 * `PebbleException` is built from, so a filter that rejects its input can point at the template line that
 * wrote it.
 */
class PebbleFilterExecutionContext(
    val name: String,
    val args: Map<String, Any?>,
    val template: PebbleTemplate,
    val context: EvaluationContext,
    val lineNumber: Int
)

/**
 * Helper to create a [PebbleFilter] with a lambda, the filter-side equivalent of
 * [invirt.pebble.functions.pebbleFunction].
 *
 * The filtered value is the lambda's argument and the named arguments are in
 * [PebbleFilterExecutionContext.args]. A null input reaches the lambda as null rather than short-circuiting
 * to null: Pebble applies a filter to a null value too, and whether that is nothing to do or an error is
 * the filter's own decision.
 */
fun pebbleFilter(
    name: String,
    vararg argumentNames: String,
    apply: PebbleFilterExecutionContext.(input: Any?) -> Any?
): PebbleFilter = object : PebbleFilter(name) {
    override fun getArgumentNames(): List<String> = argumentNames.toList()

    override fun apply(
        input: Any?,
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any? = apply.invoke(
        PebbleFilterExecutionContext(
            name = name,
            args = args,
            template = self,
            context = context,
            lineNumber = lineNumber
        ),
        input
    )
}
