package invirt.pebble

import invirt.pebble.functions.PebbleFunction
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Extension
import io.pebbletemplates.pebble.extension.Function

fun pebbleFunctions(vararg functions: PebbleFunction): Extension = object : AbstractExtension() {
    override fun getFunctions(): Map<String, Function> = functions.toList().associateBy { it.name }
}
