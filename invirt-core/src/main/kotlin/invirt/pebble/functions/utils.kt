package invirt.pebble.functions

import invirt.utils.minorUnitToString
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.EvaluationContextImpl
import org.http4k.core.Request
import java.util.*

fun EvaluationContext.findObject(key: String): Any? {
    val scope = (this as EvaluationContextImpl).scopeChain.globalScopes.find { it.keys.contains(key) }
    return scope?.get(key)
}

val EvaluationContext.request: Request get() = findObject("request") as Request

/**
 * Used mainly for access inside macros, otherwise request is exposed
 * in the current model for a template
 */
val requestFunction = pebbleFunction("request") {
    context.request
}

/**
 * Used mainly for access inside macros, otherwise errors is exposed
 * in the current model for a template
 */
val errorsFunction = pebbleFunction("errors") {
    context.findObject("errors")
}

val currencyFromMinorUnitFunction = pebbleFunction("currencyFromMinorUnit", "minorUnitAmount", "currency") {
    val minorUnitAmount = (args["minorUnitAmount"]!! as Number).toLong()
    Currency.getInstance(args["currency"]!! as String).minorUnitToString(minorUnitAmount)
}

val pluralizeFunction = pebbleFunction("pluralize", "count", "singular", "plural") {
    val count = (args["count"] as Number).toLong()
    val singular = args["singular"] as String
    val plural = args["plural"] as String
    if (count == 1L) singular else plural
}
