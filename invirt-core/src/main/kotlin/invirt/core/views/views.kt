package invirt.core.views

import invirt.core.Invirt
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.ResponseWithContext
import org.http4k.template.ViewModel

/**
 * Renders the specified template with the given model object.
 */
fun renderTemplate(template: String): Response {
    val viewModel = object : ViewModel {
        override fun template() = template
    }
    return viewModel.ok()
}

/**
 * A response that renders a view (template) from a model object and a status OK.
 */
fun ViewModel.ok(): Response = this.status(Status.OK)

/**
 * A response that renders a view (template) from a model object and a status.
 */
fun ViewModel.status(status: Status): Response =
    ResponseWithContext(Response(status).with(Invirt.viewLens of this), mapOf("viewModel" to this))

/**
 * Convenience implementation for a [ViewModel] that renders a view (template)
 * and provides the template name in the constructor.
 */
open class ViewResponse(val template: String) : ViewModel {
    override fun template() = template
}
