package examples.data.handlers

import examples.data.model.Order
import examples.data.model.OrderStatus
import examples.data.service.OrderService
import invirt.core.GET
import invirt.core.data.page
import invirt.core.data.queryDataFilter
import invirt.core.data.sort
import invirt.core.views.InvirtView
import invirt.core.views.ok
import invirt.data.RecordsPage
import invirt.data.Sort
import invirt.data.eq
import invirt.data.gt
import invirt.data.lt
import org.http4k.lens.Query
import org.http4k.lens.enum
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object OrderHandler {

    operator fun invoke(orderService: OrderService): RoutingHttpHandler = routes(
        "/" GET { request ->
            val filter = ordersFilter(request)
            val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
            val page = request.page()

            val ordersPage = orderService.searchOrders(filter, sort, page)
            ListOrdersResponse(ordersPage).ok(request)
        }
    )
}

private val ordersFilter = queryDataFilter {
    Query.optional("total-order-value").filter { value ->
        when (value) {
            "less-than-1000" -> Order::totalMinorUnit.lt(1000_00)
            "more-than-1000" -> Order::totalMinorUnit.gt(1000_00)
            else -> null
        }
    }

    Query.enum<OrderStatus>().multi.optional("status").or { status ->
        Order::status.eq(status)
    }
}

private class ListOrdersResponse(
    val ordersPage: RecordsPage<Order>
) : InvirtView("list-orders") {

    val orderStatusValues = OrderStatus.entries
}
