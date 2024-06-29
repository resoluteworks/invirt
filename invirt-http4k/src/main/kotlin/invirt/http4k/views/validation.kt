package invirt.http4k.views

import io.validk.ValidationErrors
import org.http4k.core.Response
import org.http4k.core.Status

internal class ErrorResponseView(
    val model: Any,
    val errors: ValidationErrors,
    val view: String
) : ViewResponse(view)

fun Any.errorResponse(
    errors: ValidationErrors,
    template: String,
    status: Status = Status.UNPROCESSABLE_ENTITY
): Response = ErrorResponseView(this, errors, template).status(status)
