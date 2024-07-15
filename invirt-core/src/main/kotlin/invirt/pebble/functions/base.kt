package invirt.pebble.functions

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate

abstract class PebbleFunction(val name: String) : Function

abstract class NoArgsPebbleFunction(name: String) : PebbleFunction(name) {
    override fun getArgumentNames() = emptyList<String>()
}

class PebbleFunctionExecutionContext(
    val args: Map<String, Any>,
    val template: PebbleTemplate,
    val context: EvaluationContext,
    val lineNumber: Int
)

fun pebbleFunction(name: String, vararg argumentNames: String, block: PebbleFunctionExecutionContext.() -> Any?): PebbleFunction {
    return object : PebbleFunction(name) {
        override fun getArgumentNames() = argumentNames.toList()

        override fun execute(args: MutableMap<String, Any>, self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any? {
            return block(
                PebbleFunctionExecutionContext(
                    args = args,
                    template = self,
                    context = context,
                    lineNumber = lineNumber
                )
            )
        }
    }
}
