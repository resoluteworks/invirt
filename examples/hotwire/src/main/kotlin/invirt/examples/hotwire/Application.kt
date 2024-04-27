package invirt.examples.hotwire

import invirt.http4k.AppRequestContexts
import invirt.http4k.GET
import invirt.http4k.POST
import invirt.http4k.ViewResponse
import invirt.http4k.Views
import invirt.http4k.filters.CatchAllFilter
import invirt.http4k.ok
import invirt.http4k.renderTemplate
import invirt.http4k.setDefaultViewLens
import invirt.http4k.toForm
import invirt.http4k.turboStream
import invirt.http4k.turboStreamRefresh
import invirt.http4k.views.errorResponse
import invirt.http4k.views.withView
import invirt.pebble.invirtPebbleFilter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.then
import org.http4k.lens.boolean
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Netty

private val log = KotlinLogging.logger {}

class UserListResponse(
    val users: List<User>,
    val highlightUserId: String? = null
) : ViewResponse("user-list")

class Application {

    fun start() {
        val developmentMode = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(Environment.ENV)
        setDefaultViewLens(Views(hotReload = developmentMode))

        val userService = UserService()
        val appHandler = AppRequestContexts()
            .then(CatchAllFilter())
            .then(invirtPebbleFilter)
            .then(
                routes(
                    "/" GET {
                        ("users" to userService.allUsers()) withView "index"
                    },

                    "/users/add" GET { renderTemplate("add-user") },
                    "/users/add" POST { request ->
                        request.toForm<AddUserForm>()
                            .validate {
                                error { errorResponse(it) }
                                success { form ->
                                    val user = form.createUser()
                                    userService.add(user)
                                    UserListResponse(userService.allUsers(), user.id).ok()
                                }
                            }
                    },

                    "/users/{userId}/edit" GET { request ->
                        val userId = request.path("userId")!!
                        val user = userService.getUser(userId)!!
                        EditUserForm(user).ok().turboStream()
                    },

                    "/users/{userId}/edit" POST { request ->
                        val userId = request.path("userId")!!
                        request.toForm<EditUserForm>()
                            .validate {
                                error {
                                    withUserId(userId).errorResponse(it).turboStream()
                                }
                                success { form ->
                                    userService.update(userId) { form.update(it) }
                                    turboStreamRefresh()
                                }
                            }
                    },

                    "/users/{userId}/delete" POST { request ->
                        val userId = request.path("userId")!!
                        userService.deleteUser(userId)
                        UserListResponse(userService.allUsers()).ok()
                    },
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
