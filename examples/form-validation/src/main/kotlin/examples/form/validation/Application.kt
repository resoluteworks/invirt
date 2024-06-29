package examples.form.validation

import invirt.http4k.*
import invirt.http4k.views.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.validk.ValidObject
import io.validk.Validation
import io.validk.email
import io.validk.minLength
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.then
import org.http4k.lens.boolean
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

data class SignupForm(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
) : ValidObject<SignupForm> {

    override val validation = Validation<SignupForm> {
        SignupForm::name.notNullOrBlank("Name is required") {
            minLength(5) message "Name is too short"
        }
        SignupForm::email.notNullOrBlank("Email is required") {
            email() message "Not a valid email"
        }
        SignupForm::password.notNullOrBlank("Password is required") {
            minLength(8) message "Password is too short"
        }
        withValue { form ->
            if (form.password.isNotBlank()) {
                SignupForm::password {
                    addConstraint("Passwords don't match") { form.password == form.confirmPassword }
                }
            }
        }
    }
}

class Application {

    fun start() {
        val developmentMode = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(Environment.ENV)
        setDefaultViewLens(Views(hotReload = developmentMode))

        val appHandler = AppRequestContexts()
            .then(StoreRequestOnThread())
            .then(
                routes(
                    "/" GET { renderTemplate("signup") },

                    "/signup" POST { request ->
                        request.toForm<SignupForm>()
                            .validate {
                                error { errors ->
                                    errorResponse(errors, "signup")
                                }
                                success { form ->
                                    // Signup user with this form
                                    httpSeeOther("/organiser/profile?saved=true")
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
