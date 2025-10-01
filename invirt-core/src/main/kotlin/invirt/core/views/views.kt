package invirt.core.views

import invirt.core.Invirt
import io.validk.ValidationErrors
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.ResponseWithContext

/**
 * Renders the specified [template] with the given [model] object.
 */
fun renderTemplate(
    request: Request,
    template: String,
    model: Any? = null
): Response = viewResponse(
    request = request,
    model = model,
    template = template,
    errors = null,
    status = Status.OK
)

/**
 * A response that renders a view (template) from a model object and a status OK.
 */
fun InvirtView.ok(request: Request): Response = this.status(request, Status.OK)

/**
 * A response that renders a view (template) from a model object and a status.
 */
fun InvirtView.status(request: Request, status: Status): Response = viewResponse(request, this, this.template, null, status)

/**
 * Renders the specified [template] with the given [model] object and optional validation [errors].
 */
internal fun viewResponse(
    request: Request,
    model: Any?,
    template: String,
    errors: ValidationErrors?,
    status: Status
): Response {
    val invirtRenderModel = InvirtRenderModel(request, template, model, errors)
    return ResponseWithContext(Response(status).with(Invirt.viewLens of invirtRenderModel), mapOf("invirtRenderModel" to invirtRenderModel))
}
