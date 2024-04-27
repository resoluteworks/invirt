package invirt.http4k.views

import invirt.http4k.AppRequestContexts
import invirt.http4k.ViewResponse
import invirt.http4k.status
import io.validk.ValidationErrors
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextKey
import org.http4k.template.ViewModel

/**
 * Used strictly to provide these errors within the context of a macro
 */
internal val validationErrorContextKey = RequestContextKey.optional<ValidationErrors>(AppRequestContexts.contexts)

internal fun Request.setErrors(errors: ValidationErrors) {
    validationErrorContextKey[this] = errors
}

class ErrorResponseView(
    val model: Any,
    val errors: ValidationErrors,
    val view: String
) : ViewResponse(view)

fun ViewModel.errorResponse(errors: ValidationErrors, status: Status = Status.UNPROCESSABLE_ENTITY): Response {
    return ErrorResponseView(this, errors, this.template()).status(status)
}
