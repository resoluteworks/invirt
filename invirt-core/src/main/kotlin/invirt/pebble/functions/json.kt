package invirt.pebble.functions

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

private val jsonMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

/**
 * Renders a Java object a JSON object. Used to render objects in a JavaScript context
 */
val jsonFunction = pebbleFunction("json", "value") {
    val obj = args["value"]!!
    jsonMapper.writeValueAsString(obj)
}

/**
 * Renders a Java collection or object as a JSON array. Used to render arrays in a JavaScript context
 */
val jsonArrayFunction = pebbleFunction("jsonArray", "value") {
    val obj = args["value"]!!
    if (obj is Collection<*>) {
        jsonMapper.writeValueAsString(obj)
    } else {
        jsonMapper.writeValueAsString(listOf(obj))
    }
}
