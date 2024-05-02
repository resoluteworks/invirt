package invirt.http4k.filters

import invirt.http4k.views.renderTemplate
import org.http4k.core.Filter
import org.http4k.core.Status

/**
 * Renders a view based on a status code
 */
object ErrorPages {

    operator fun invoke(vararg statusViewMappings: Pair<Status, String>): Filter {
        return invoke(statusViewMappings.toMap())
    }

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
