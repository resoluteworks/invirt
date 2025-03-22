package examples.form.validation

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.POST
import invirt.core.httpSeeOther
import invirt.core.toForm
import invirt.core.views.errorResponse
import invirt.core.views.renderTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import io.validk.ValidObject
import io.validk.Validation
import io.validk.constraints.email
import io.validk.constraints.minLength
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

data class SignupForm(
    val name: String,
    val email: String,
    val password: String
) : ValidObject<SignupForm> {

    override val validation = Validation {
        SignupForm::name.notNullOrBlank("Name is required") {
            minLength(5) message "Name is too short"
        }
        SignupForm::email.notNullOrBlank("Email is required") {
            email() message "Not a valid email"
        }
        SignupForm::password.notNullOrBlank("Password is required") {
            minLength(8) message "Password is too short"
        }
    }
}

class Application {

    fun start() {
        val appHandler = Invirt()
            .then(
                routes(
                    "/" GET { renderTemplate("signup") },
                    "/signup/success" GET { renderTemplate("signup-success") },

                    "/signup" POST { request ->
                        request.toForm<SignupForm>()
                            .validate()
                            .map {
                                error { form, errors ->
                                    errorResponse(form, errors, "signup.peb")
                                }
                                success { form ->
                                    // TODO: Create user, sign up, etc.
                                    httpSeeOther("/signup/success")
                                }
                            }
                    }
                )
            )

        val server = Netty(8080).toServer(appHandler)
        server.start()
        log.info { "Server started at http://localhost:${server.port()}" }
    }
}

fun main() {
    Application().start()
}
