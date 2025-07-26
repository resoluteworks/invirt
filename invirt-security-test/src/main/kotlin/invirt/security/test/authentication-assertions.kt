package invirt.security.test

import invirt.security.authentication.AuthenticationResponse
import invirt.security.authentication.Principal
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Extension function to assert that an [AuthenticationResponse] is successful.
 * It checks if the response is of type [AuthenticationResponse.Authenticated].
 *
 * @param P The type of the Principal.
 * @return The [AuthenticationResponse.Authenticated] instance if the assertion passes.
 * @throws AssertionError if the response is not of type [AuthenticationResponse.Authenticated].
 */
fun <P : Principal> AuthenticationResponse.shouldBeSuccessful(): AuthenticationResponse.Authenticated<P> {
    this.shouldBeInstanceOf<AuthenticationResponse.Authenticated<P>>()
    return this
}

/**
 * Extension function to assert that an [AuthenticationResponse] is not successful.
 * It checks if the response is of type [AuthenticationResponse.Unauthenticated].
 */
fun AuthenticationResponse.shouldNotBeSuccessful() {
    this.shouldBeInstanceOf<AuthenticationResponse.Unauthenticated>()
}
