package invirt.pebble.functions

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate

abstract class PebbleFunction(val name: String) : Function

class PebbleFunctionExecutionContext(
    val args: Map<String, Any>,
    val template: PebbleTemplate,
    val context: EvaluationContext,
    val lineNumber: Int
)

/**
 * Helper to create a [PebbleFunction] with a lambda
 */
fun pebbleFunction(
    name: String,
    vararg argumentNames: String,
    block: PebbleFunctionExecutionContext.() -> Any?
): PebbleFunction = object : PebbleFunction(name) {
    override fun getArgumentNames() = argumentNames.toList()

    override fun execute(args: MutableMap<String, Any>, self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any? = block(
        PebbleFunctionExecutionContext(
            args = args,
            template = self,
            context = context,
            lineNumber = lineNumber
        )
    )
}
