package invirt.http4k.views

import invirt.http4k.AppRequestContexts
import io.validk.ValidationErrors
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextKey

/**
 * Used strictly to provide these errors within the context of a macro
 */
internal val validationErrorContextKey = RequestContextKey.optional<ValidationErrors>(AppRequestContexts.contexts)

internal fun Request.setErrors(errors: ValidationErrors) {
    validationErrorContextKey[this] = errors
}

internal class ErrorResponseView(
    val model: Any,
    val errors: ValidationErrors,
    val view: String
) : ViewResponse(view)

fun Any.errorResponse(
    errors: ValidationErrors,
    template: String,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response {
    return ErrorResponseView(this, errors, template).status(status)
}
