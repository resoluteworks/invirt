package invirt.core.filters

import invirt.core.views.renderTemplate
import org.http4k.core.Filter
import org.http4k.core.Status

/**
 * An http4k filter that renders a view (template) for a given status code.
 */
object ErrorPages {

    operator fun invoke(vararg statusViewMappings: Pair<Status, String>): Filter = invoke(statusViewMappings.toMap())

    operator fun invoke(statusViewMappings: Map<Status, String>): Filter = Filter { next ->
        { request ->
            val response = next(request)
            val view = statusViewMappings[response.status]
            if (view != null) {
                renderTemplate(view).status(response.status)
            } else {
                response
            }
        }
    }
}
