---
sidebar_position: 2
---

import image1 from './assets/example-app-1.png';

# Example application

This [example](https://github.com/resoluteworks/invirt/tree/main/examples/data-querying)
combines capabilities for filters, sorting and pagination in one Invirt application. The setup
is a (very crude) screen to review online orders with the following requirements:
* The user can filter orders by their total value: less than £1000, greater than £1000, or "All" (no filtering applied).
Only one of the three can be applied as they are mutually exclusive. By default, all orders are listed ("All").
* The user can filter orders by their status (Dispatched, Delivered, etc). Any or all of these can be
selected for filtering and the page will present the combined result set (in essence an `OR`).
* Filtering will apply an `AND` between the order value criteria and the order status criteria, i.e. the orders
returned must match the selected total value, as well as matching any selected order statuses.
* The user can sort the result set ascending and descending by clicking the listing column headers (Created at, Order status, Total value)
* The results are paginated (page size 10) and a pagination control allows the user to navigate the complete
result set.

<img src={image1} width="600"/>

## Query parameters
The query parameter for filtering with the above criteria is defined as follows:
 * A `total` query parameter specifies the filtering to be applied for the total order value
    * A value of `less-than-1000` would list orders with a total value less than £1000
    * A value of `more-than-1000` would list orders with a total value greater than £1000
    * A missing parameter indicates no filter should be applied for the total order value
 * A `status` query parameter specifies the statuses of the orders to return. This can be combined
   by passing the parameter multiple times: `?status=DELIVERED&status=IN_TRANSIT`
 * A `sort` query parameter specifies the sort order.
   * `createdAt:ASC|DESC` - sort orders by their creation timestamp
   * `status:ASC|DESC` - sort orders by their current status
   * `totalMinorUnit:ASC|DESC` - sort orders by their current total value in minor currency units (pence for GBP, cents for USD, etc)
   * When the `sort` parameter is missing, the search defaults to `createdAt:DESC`
 * Pagination information is passed using the `from` and `size` query parameters: `&from=0&size=10`

## Core components definition
An `OrderService` exposes a function that allows the application to search orders based on a filtering criteria,
a sort order, and a pagination constraint. This function returns a [RecordsPage](/docs/framework/data-querying/pagination#recordspage).
```kotlin
class OrderService {
    fun searchOrders(filter: DataFilter?, sort: Sort, page: Page): RecordsPage<Order> {
        // Search in an orders repository and return a RecordsPage<Order>
    }
}

```

An `OrderHandler` maps the default (`/`) route to the page in the screenshot above.
```kotlin
object OrderHandler {

    operator fun invoke(orderService: OrderService): RoutingHttpHandler = routes(
        "/" GET { request ->
            // Get filters/sort/pagination from request parameters
            // and call OrderService.searchOrders()
        }
    )
}
```

## Handler implementation
The handler is responsible for reading the query parameters defined above and creating the relevant
objects to be passed to the `OrderService`. Starting with the easier ones, we can use Invirt's built-in
extensions to read [sort](/docs/framework/data-querying/sort#sort-in-query-parameters) and
[page](/docs/framework/data-querying/pagination#page-from-query-parameters) information from the request's query parameters.

```kotlin
val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
val page = request.page()
```
### Filtering logic
For filtering we need to first define the handling of the filter query parameters `total` and `status`
based on the logic described earlier. For this, we will use Invirt's built-in `queryValuesFilter`.
By default, this function builds an `AND` compound filter based on the individual filters defined
within its lambda.

```kotlin
private val filter = queryValuesFilter {
   // Only the first "total" param in the query is handled (there should be only one).
   //
   // .filter specifies a lambda returning a DataFilter for a given parameter value.
   //
   // Returning null indicates that there is no filter to apply.
   Query.optional("total").filter { value ->
      when (value) {
         "less-than-1000" -> Order::totalMinorUnit.lt(1000_00)
         "more-than-1000" -> Order::totalMinorUnit.gt(1000_00)
         else -> null
      }
   }

   // .multi (an http4k built-in) indicates that this parameter can appear
   // multiple times in a query: &status=DELIVERED&status=IN_TRANSIT
   //
   // Because it's a multi-param, we must specify an [.and] or [.or]
   // to indicate how the multiple values will be combined in the final filter.
   //
   // The lambda defines what filter should be produced for each individual value,
   // and a compound (OR, in this case) filter will be returned based on these
   // individual value filters.
   Query.enum<OrderStatus>().multi.optional("status").or { status ->
      Order::status.eq(status)
   }
}
```
### Listing response and view wiring

### Complete handler code
```kotlin
object OrderHandler {

    operator fun invoke(orderService: OrderService): RoutingHttpHandler = routes(
        "/" GET { request ->
            val filter = filter(request)
            val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
            val page = request.page()

            val ordersPage = orderService.searchOrders(filter, sort, page)
            ListOrdersResponse(ordersPage).ok()
        }
    )
}

private val filter = queryValuesFilter {
    Query.optional("total").filter { value ->
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

private class ListOrdersResponse(val ordersPage: RecordsPage<Order>) : ViewResponse("list-orders") {
    val orderStatusValues = OrderStatus.entries
}
```

## OrderService
For this
