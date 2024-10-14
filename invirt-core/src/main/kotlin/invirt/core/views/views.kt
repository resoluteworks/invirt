package invirt.core.views

import invirt.core.Invirt
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.template.ViewModel

/**
 * Renders the specified template with the given model object.
 */
fun renderTemplate(template: String): Response = Response(Status.OK).with(
    Invirt.viewLens of object : ViewModel {
        override fun template() = template
    }
)

/**
 * A response that renders a view (template) from a model object and a status OK.
 */
fun ViewModel.ok(): Response = this.status(Status.OK)

/**
 * A response that renders a view (template) from a model object and a status.
 */
fun ViewModel.status(status: Status): Response = Response(status).with(Invirt.viewLens of this)

/**
 * Convenience class for a [ViewModel] that renders a view (template).
 */
open class ViewResponse(val template: String) : ViewModel {
    override fun template() = template
}
