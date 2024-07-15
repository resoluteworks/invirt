package invirt.http4k.views

import org.http4k.core.Response

class MapView(
    map: Map<String, Any?>,
    private val view: String
) : ViewResponse(view), Map<String, Any?> by map {
    override fun toString(): String {
        return view
    }
}

infix fun Map<String, Any?>.withView(view: String): Response = MapView(this, view).ok()
infix fun Pair<String, Any?>.withView(view: String): Response = mapOf(this).withView(view)
