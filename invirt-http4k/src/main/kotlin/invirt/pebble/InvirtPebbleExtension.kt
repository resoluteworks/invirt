package invirt.pebble

import invirt.http4k.requestScopeValue
import invirt.pebble.filters.DateWithDaySuffixFilter
import invirt.pebble.functions.*
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
            toggleQueryValueFunction
        ).associateBy { it.name }
    }

    override fun getFilters(): Map<String, Filter> {
        return mapOf(
            "dateWithDaySuffix" to DateWithDaySuffixFilter()
        )
    }
}
