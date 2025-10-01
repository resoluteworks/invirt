package invirt.core.views

import io.validk.ValidationErrors
import org.http4k.core.Request

data class InvirtRenderModel(
    val request: Request,
    val template: String,
    val model: Any? = null,
    val errors: ValidationErrors? = null
)
