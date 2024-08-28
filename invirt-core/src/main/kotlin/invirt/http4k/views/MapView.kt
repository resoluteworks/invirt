package invirt.http4k.views

import org.http4k.core.Response

/**
 * A response that renders a view (template) from a map of model values.
 */
class MapView(
    map: Map<String, Any?>,
    private val view: String
) : ViewResponse(view), Map<String, Any?> by map {
    override fun toString(): String = view
}

/**
 * A response that renders a view (template) from a model represented as a map.
 */
infix fun Map<String, Any?>.withView(view: String): Response = MapView(this, view).ok()

/**
 * A response that renders a view (template) from a model represented as a single key-value pair.
 */
infix fun Pair<String, Any?>.withView(view: String): Response = mapOf(this).withView(view)
