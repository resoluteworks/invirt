package invirt.pebble

import invirt.pebble.filters.DateWithDaySuffixFilter
import invirt.pebble.functions.currencyFromMinorUnitFunction
import invirt.pebble.functions.errorsFunction
import invirt.pebble.functions.jsonArrayFunction
import invirt.pebble.functions.jsonFunction
import invirt.pebble.functions.pebbleFunction
import invirt.pebble.functions.requestFunction
import invirt.utils.uuid7
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.extension.Function
import java.time.LocalDate

class InvirtPebbleExtension(private val staticAssetsVersion: String? = null) : AbstractExtension() {

    override fun getGlobalVariables(): Map<String, Any?> = mapOf("staticAssetsVersion" to staticAssetsVersion)

    override fun getFunctions(): Map<String, Function> = listOf(
        pebbleFunction("today") { LocalDate.now() },
        pebbleFunction("uuid") { uuid7() },

        // Utils
        currencyFromMinorUnitFunction,
        errorsFunction,

        // JSON
        jsonFunction,
        jsonArrayFunction,

        // Request
        requestFunction
    ).associateBy { it.name }

    override fun getFilters(): Map<String, Filter> = mapOf(
        "dateWithDaySuffix" to DateWithDaySuffixFilter()
    )
}
