package examples.data.handlers

import examples.data.model.Order
import examples.data.model.OrderStatus
import examples.data.repository.OrderRepository
import invirt.data.RecordsPage
import invirt.data.Sort
import invirt.data.eq
import invirt.data.gt
import invirt.data.lt
import invirt.http4k.GET
import invirt.http4k.data.page
import invirt.http4k.data.queryValuesFilter
import invirt.http4k.data.sort
import invirt.http4k.views.ViewResponse
import invirt.http4k.views.ok
import org.http4k.lens.Query
import org.http4k.lens.enum
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object OrderHandler {

    operator fun invoke(orderRepository: OrderRepository): RoutingHttpHandler = routes(
        "/" GET { request ->
            val filter = filter(request)
            val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
            sort.revert()
            val page = request.page()

            val ordersPage = orderRepository.searchOrders(filter, sort, page)
            ListOrdersResponse(ordersPage).ok()
        }
    )
}

private val filter = queryValuesFilter {
    Query.enum<OrderStatus>().multi.optional("status").or { status ->
        Order::status.eq(status)
    }

    Query.optional("total").filter { value ->
        when (value) {
            "less-than-1000" -> Order::totalMinorUnit.lt(100000)
            "more-than-1000" -> Order::totalMinorUnit.gt(100000)
            else -> null
        }
    }
}

private class ListOrdersResponse(val ordersPage: RecordsPage<Order>) : ViewResponse("list-orders") {

    val orderStatusValues = OrderStatus.entries
}
