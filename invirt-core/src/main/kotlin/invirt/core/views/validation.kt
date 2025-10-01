package invirt.core.views

import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Creates a [Response] from the given [InvirtView] and the specified validation [errors].
 * This function treats the [InvirtView] as the model object to be rendered.
 */
fun InvirtView.asErrorResponse(
    request: Request,
    errors: ValidationErrors,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response = errorResponse(request, errors, this.template, this, status)

/**
 * Creates a [Response] from the given [model] object and the specified validation [errors]
 * and renders it with the given [template].
 */
fun errorResponse(
    request: Request,
    errors: ValidationErrors,
    template: String,
    model: Any? = null,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response = viewResponse(
    request = request,
    model = model,
    template = template,
    errors = errors,
    status = status
)

/**
 * Creates a [Response] from the given key-value pair representing a model object
 * and the specified validation [errors] and renders it with the given [template].
 */
fun errorResponse(
    request: Request,
    template: String,
    vararg errors: Pair<String, String>
): Response {
    if (errors.isEmpty()) {
        throw IllegalArgumentException("Errors cannot be empty")
    }
    return errorResponse(
        request = request,
        errors = ValidationErrors(errors.map { ValidationError(it.first, it.second) }),
        template = template,
        model = null,
        status = Status.UNPROCESSABLE_ENTITY
    )
}
