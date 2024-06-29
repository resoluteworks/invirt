package examples.form.basics

import invirt.http4k.GET
import invirt.http4k.InvirtRequestContext
import invirt.http4k.POST
import invirt.http4k.toForm
import invirt.http4k.views.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.then
import org.http4k.lens.boolean
import org.http4k.routing.routes
import org.http4k.server.Netty
import java.time.LocalDate

private val log = KotlinLogging.logger {}

data class OrderForm(
    val name: String,
    val email: String,
    val deliveryDetails: DeliveryDetails,
    val notifications: Set<NotificationType>,
    val quantities: Map<String, Int>
)

data class DeliveryDetails(
    val whenNotAtHome: String,
    val deliveryDate: LocalDate
)

data class User(val name: String)

data class ListUsersResponse(
    val users: List<User>
) : ViewResponse("users/list")

enum class NotificationType(val label: String) {
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In transit"),
    DELIVERED("Delivered")
}

class OrderSaved(
    val order: OrderForm
) : ViewResponse("order-saved.peb")

class Application {

    fun start() {
        val developmentMode = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(Environment.ENV)
        setDefaultViewLens(Views(hotReload = developmentMode))

        val appHandler = InvirtRequestContext().then(
            routes(
                "/" GET { renderTemplate("create-order") },

                "/save-order" POST { request ->
                    val form = request.toForm<OrderForm>()
                    log.info { "Submitted form: $form" }
                    OrderSaved(form).ok()
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
