package invirt.http4k.views

import io.validk.ValidationErrors
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.template.ViewModel

internal class ErrorResponseView(
    val model: Any,
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
    model: Any,
    errors: ValidationErrors,
    template: String,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response = ErrorResponseView(model, errors, template).status(status)
