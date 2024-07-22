---
sidebar_position: 2
---

import image1 from './assets/example-app-1.png';
import image2 from './assets/example-app-2.png';

# Example application

This [example](https://github.com/resoluteworks/invirt/tree/main/examples/data-querying)
combines capabilities for filters, sorting and pagination in one Invirt application. The setup
is a (very crude) screen to review online orders with the following requirements:
* The user can filter orders by their total value: less than £1000, greater than £1000, or "All" (no filtering applied).
Only one of the three can be applied as they are mutually exclusive. By default, all orders are listed ("All").
* The user can filter orders by their status (Dispatched, Delivered, etc). Any or all of these can be
selected for filtering and the page will present the combined result set (in essence an `OR`).
* Filtering will apply an `AND` between the total order value criteria and the order status criteria, i.e. the orders
returned must match the selected total value, as well as matching any selected order statuses.
* The user can sort the result set ascending and descending by clicking the listing column headers (Created at, Order status, Total value)
* The results are paginated (page size 10) and a pagination control allows the user to navigate the complete
result set.

<img src={image1} width="600"/>

## Query parameters
The query parameter for filtering with the above criteria is defined as follows:
 * A `total-order-value` query parameter specifies the filtering to be applied for the total order value
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
a sort order, and a pagination constraint. This function returns a [RecordsPage](/docs/api/invirt-data/page#recordspage).
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
extensions to read [sort](/docs/api/invirt-core/request-extensions#requestsort) and
[page](/docs/api/invirt-core/request-extensions#requestpage) information from the request's query parameters.

```kotlin
val sort = request.sort() ?: Sort.desc(Order::createdAt.name)
val page = request.page()
```
### Filtering logic
For filtering we need to first define the handling of the filter query parameters `total-order-value` and `status`
based on the logic described earlier. For this, we will use Invirt's built-in `queryValuesFilter`.

```kotlin
val ordersFilter = queryValuesFilter {
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
```

Several things to unpack here, so let's start with `queryValuesFilter()` itself.
This function builds a `QueryValuesFilter` object which stores the configuration
(lambdas) how to transform query parameters into[DataFilter](/docs/api/invirt-data/data-filter)
objects at runtime. This object can then be applied to a `Request` to produce the final
`DataFilter`, or `null` if none of the expected parameters are present.

```kotlin
val ordersFilter = queryValuesFilter {...}

"/" GET { request ->
   val filter: DataFilter? = ordersFilter(request)
   ...
}
```

The constructs inside the lambda will use http4k's built-in [parameter lensing](https://www.http4k.org/guide/howto/typesafe_your_api_with_lenses/)
to start defining an expression for processing a parameter, hence the `Query.` chained calls.

By default, `queryValuesFilter` builds an `AND` compound filter from the individual parameter filters defined in its lambda.
This can be overridden by passing `DataFilter.Compound.Operator.OR`.
```kotlin
queryValuesFilter { ... } // Defaults to DataFilter.Compound.Operator.AND
queryValuesFilter(DataFilter.Compound.Operator.AND) { ... }
queryValuesFilter(DataFilter.Compound.Operator.OR) { ... }
```

#### Handling total-order-value
`total-order-value` is handled as a query parameter that is only passed (and handled) once, i.e. passing the query
below will cause the second value to be ignored, which is what we want in this case.
```
&total-order-value=less-than-1000&total-order-value=more-than-1000
```

To convert `total-order-value` to a `DataFilter` we start with http4k built-in `Query.optional("total-order-value")`
and then call Invirt's `.filter` to specify the lambda returning a `DataFilter` for the current value of this parameter.
In our case, we have `when` clause defining explicitly what filter is produced for each value
(or `null` if the passed value doesn't match any of them).

#### Handling status
Http4k's built-ins lensing is used again to convert the parameter to `OrderStatus` enum values and specify that it can
appear multiple times in the query (`.multi`).
```kotlin
Query.enum<OrderStatus>().multi.optional("status")
```

This then allows us to call an Invirt extension (`.or` in this case) to specify
 * How the individual `DataFilters` must be combined when there are multiple values which: OR or AND (`.or` / `.and`). For our
   requirements we need an OR for the order status filter.
 * How each of the individual `OrderStatus` convert to a `DataFilter`, a simple equals filter, in this case, via `Order::status.eq(status)`

### Response and view wiring
To render the orders we will use [ViewResponse](/docs/framework/views-wiring#viewresponse), which will store
the `RecordsPage<Order>` returned by `OrderService.searchOrders()`, to be consumed directly by the template.

We also want to provide the `OrderStatus` enum values to render the possible options for the order status
filter (see image below), so we don't hardcode these in the template which can cause them to drift from the internal enum definition.

<img src={image2} width="600"/>

The code for this component would then be fairly straightforward.
```kotlin
private class ListOrdersResponse(
    val ordersPage: RecordsPage<Order>
) : ViewResponse("list-orders") {

    val orderStatusValues = OrderStatus.entries
}
```

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

private class ListOrdersResponse(val ordersPage: RecordsPage<Order>) : ViewResponse("list-orders") {
    val orderStatusValues = OrderStatus.entries
}
```

## OrderService
For the OrderService we've used a mock implementation using a local list of random orders, which we then
sort, filter and paginate in memory. This is done to avoid wiring a persistence layer for this example,
and to be able to demonstrate the concept and the separation of concerns in its raw form. Because this isn't
something that any application would normally do, we won't go into the details of this, but you can check out the complete
code for it [here](https://github.com/resoluteworks/invirt/blob/main/examples/data-querying/src/main/kotlin/examples/data/service/OrderService.kt).
