package examples.data.basics.handlers

import examples.data.basics.model.Order
import examples.data.basics.repository.OrderRepository
import invirt.data.RecordsPage
import invirt.data.Sort
import invirt.http4k.GET
import invirt.http4k.data.page
import invirt.http4k.data.sort
import invirt.http4k.views.ViewResponse
import invirt.http4k.views.ok
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object OrderHandler {

    operator fun invoke(orderRepository: OrderRepository): RoutingHttpHandler = routes(
        "/" GET { request ->
            val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
            val page = request.page(defaultFrom = 0, defaultSize = 15, maxSize = 15)

            ListOrdersResponse(
                ordersPage = orderRepository.searchOrders(null, sort, page),
                sort = sort
            ).ok()
        }
    )
}

private class ListOrdersResponse(
    val ordersPage: RecordsPage<Order>,
    val sort: Sort
) : ViewResponse("list-orders")
