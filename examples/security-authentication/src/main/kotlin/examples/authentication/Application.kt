package examples.authentication

import invirt.http4k.views.initialiseInvirtViews
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

// class AppAuthenticator : Authenticator {
//
//    override fun authenticate(request: Request): AuthenticationResponse {
//        val jwtAccessToken = request.cookie(COOKIE_JWT_ACCESS_TOKEN)?.value
//            ?: return AuthenticationResponse.Unauthenticated
//    }
//
//    companion object {
//        private const val COOKIE_JWT_ACCESS_TOKEN = "jwt-access-token"
//        private const val COOKIE_JWT_REFRESH_TOKEN = "jwt-refresh-token"
//    }
// }

class Application {

    fun start() {
        initialiseInvirtViews()

//        val appHandler = InvirtFilter().then(
//            routes(
//                "/" GET {
//                    IndexResponse(currentUsername = "email@test.com").ok()
//                }
//            )
//        )
//
//        val server = Netty(8080)
//        server.toServer(appHandler).start()
//        log.info { "Server started at http://localhost:${server.port}" }
    }
}

fun main() {
    Application().start()
}
