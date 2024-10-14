package invirt.core.views

import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.template.ViewModel

/**
 * A response that renders a view (template) from a model object when an error occurs.
 * Typically used to render a form with validation errors.
 *
 * @param model The model object to render in the view.
 * @param errors The validation errors to display in the view.
 * @param template The template to render.
 */
internal class ErrorResponseView(
    val model: Any?,
    val errors: ValidationErrors,
    val template: String
) : ViewModel {
    override fun template() = template
}

/**
 * Creates an [ErrorResponseView] from the given model object and the specified validation [errors].
 * This will render a page with the specified [template] and exposed the [errors] directly into
 * the page's Pebble context to be queried for display.
 */
fun errorResponse(
    model: Any?,
    errors: ValidationErrors,
    template: String,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response = ErrorResponseView(model, errors, template).status(status)

fun errorResponse(
    template: String,
    vararg errors: Pair<String, String>
): Response {
    if (errors.isEmpty()) {
        throw IllegalArgumentException("Errors cannot be empty")
    }
    return errorResponse(null, ValidationErrors(errors.map { ValidationError(it.first, it.second) }), template)
}

fun ViewResponse.toErrorResponse(errors: ValidationErrors): Response = errorResponse(this, errors, this.template)
