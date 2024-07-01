package invirt.pebble.functions

import invirt.http4k.InvirtRequestContext
import invirt.utils.minorUnitToString
import java.util.*

val currencyFromMinorUnitFunction = pebbleFunction("currencyFromMinorUnit", "minorUnitAmount", "currency") {
    val minorUnitAmount = (args["minorUnitAmount"]!! as Number).toLong()
    Currency.getInstance(args["currency"]!! as String).minorUnitToString(minorUnitAmount)
}

val errorsFunction = pebbleFunction("errors") {
    InvirtRequestContext.errors
}

/**
 * Used mainly for access inside macros, otherwise request is exposed
 * in the current model for a template
 */
val requestFunction = pebbleFunction("request") {
    InvirtRequestContext.currentRequest
}
