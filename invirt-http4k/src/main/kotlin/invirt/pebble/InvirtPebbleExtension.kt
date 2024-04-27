package invirt.pebble

import invirt.http4k.requestScopeValue
import invirt.pebble.filters.DateWithDaySuffixFilter
import invirt.pebble.functions.currencyFromMinorUnitFunction
import invirt.pebble.functions.errorsFunction
import invirt.pebble.functions.jsonArrayFunction
import invirt.pebble.functions.jsonFunction
import invirt.pebble.functions.pebbleFunction
import invirt.pebble.functions.removeQueriesFunction
import invirt.pebble.functions.removeQueryValueFunction
import invirt.pebble.functions.replacePageFunction
import invirt.pebble.functions.replaceQueryFunction
import invirt.pebble.functions.toggleQueryValueFunction
import invirt.utils.uuid7
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.extension.Function
import java.time.LocalDate

val invirtPebbleFilter: org.http4k.core.Filter = requestScopeValue(requestThreadLocal) { it }

class InvirtPebbleExtension : AbstractExtension() {

    override fun getFunctions(): Map<String, Function> {
        return listOf(
            pebbleFunction("today") { LocalDate.now() },
            pebbleFunction("uuid") { uuid7() },

            // Utils
            currencyFromMinorUnitFunction,
            errorsFunction,

            // JSON
            jsonFunction,
            jsonArrayFunction,

            // Request query
            replaceQueryFunction,
            replacePageFunction,
            removeQueryValueFunction,
            removeQueriesFunction,
            toggleQueryValueFunction,
        ).associateBy { it.name }
    }

    override fun getFilters(): Map<String, Filter> {
        return mapOf(
            "dateWithDaySuffix" to DateWithDaySuffixFilter()
        )
    }
}
