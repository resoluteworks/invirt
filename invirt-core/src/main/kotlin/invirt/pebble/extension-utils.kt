package invirt.pebble

import invirt.pebble.filters.PebbleFilter
import invirt.pebble.functions.PebbleFunction
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Extension
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.extension.Function

fun pebbleFunctions(vararg functions: PebbleFunction): Extension = object : AbstractExtension() {
    override fun getFunctions(): Map<String, Function> = functions.toList().associateBy { it.name }
}

fun pebbleFilters(vararg filters: PebbleFilter): Extension = object : AbstractExtension() {
    override fun getFilters(): Map<String, Filter> = filters.toList().associateBy { it.name }
}
