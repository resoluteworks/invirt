package invirt.pebble.functions

import invirt.http4k.views.validationErrorContextKey
import invirt.pebble.currentHttp4kRequest
import invirt.utils.minorUnitToString
import java.util.*

val currencyFromMinorUnitFunction = pebbleFunction("currencyFromMinorUnit", "minorUnitAmount", "currency") {
    val minorUnitAmount = (args["minorUnitAmount"]!! as Number).toLong()
    Currency.getInstance(args["currency"]!! as String).minorUnitToString(minorUnitAmount)
}

val errorsFunction = pebbleFunction("errors") {
    validationErrorContextKey[currentHttp4kRequest!!]
}
