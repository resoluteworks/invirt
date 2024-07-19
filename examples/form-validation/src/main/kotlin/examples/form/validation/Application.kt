package examples.form.validation

import invirt.http4k.*
import invirt.http4k.config.developmentMode
import invirt.http4k.views.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.validk.ValidObject
import io.validk.Validation
import io.validk.email
import io.validk.minLength
import org.http4k.cloudnative.env.Environment
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
        initialiseInvirtViews(hotReload = Environment.ENV.developmentMode)

        val appHandler = InvirtFilter()
            .then(
                routes(
                    "/" GET { renderTemplate("signup") },
                    "/signup/success" GET { renderTemplate("signup-success") },

                    "/signup" POST { request ->
                        request.toForm<SignupForm>()
                            .validate {
                                error { form, errors ->
                                    errorResponse(form, errors, "signup.peb")
                                }
                                success { form ->
                                    // Signup user with this form
                                    httpSeeOther("/signup/success")
                                }
                            }
                    }
                )
            )

        val server = Netty(8080)
        server.toServer(appHandler).start()
        log.info { "Server started at http://localhost:${server.port}" }
    }
}

fun main() {
    Application().start()
}
