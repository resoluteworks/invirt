package examples.data.service

import examples.data.model.Order
import examples.data.model.OrderStatus
import invirt.data.DataFilter
import invirt.data.Page
import invirt.data.RecordsPage
import invirt.data.Sort
import invirt.data.SortOrder
import invirt.data.page
import invirt.utils.minusDays
import java.time.Instant
import kotlin.random.Random

/**
 * A mock implementation for an order service + repository.
 */
class OrderService {

    private val orders = randomOrders()

    fun searchOrders(filter: DataFilter?, sort: Sort, page: Page): RecordsPage<Order> {
        val sortedOrders = if (sort.order == SortOrder.ASC) {
            orders.sortedBy { sort.field(it) }
        } else {
            orders.sortedByDescending { sort.field(it) }
        }

        val filteredOrders = sortedOrders.filter { filter?.matches(it) ?: true }
        return RecordsPage(
            records = filteredOrders.page(page),
            totalCount = filteredOrders.size.toLong(),
            page = page
        )
    }
}

private fun DataFilter.matches(order: Order): Boolean = when (this) {
    is DataFilter.Field -> this.fieldMatches(order)
    is DataFilter.And -> this.filters.all { it.matches(order) }
    is DataFilter.Or -> this.filters.any { it.matches(order) }
}

private fun DataFilter.Field.fieldMatches(order: Order): Boolean = when (this) {
    is DataFilter.Field.Eq<*> -> when (field) {
        Order::status.name -> order.status == value
        else -> throw UnsupportedOperationException("Eq not implemented for field $field")
    }

    is DataFilter.Field.Gt<*> -> when (field) {
        Order::totalMinorUnit.name -> order.totalMinorUnit > value as Long
        else -> throw UnsupportedOperationException("Gt not implemented for field $field")
    }

    is DataFilter.Field.Lt<*> -> when (field) {
        Order::totalMinorUnit.name -> order.totalMinorUnit < value as Long
        else -> throw UnsupportedOperationException("Lt not implemented for field $field")
    }

    is DataFilter.Field.Gte<*> -> when (field) {
        Order::createdAt.name -> !order.createdAt.isBefore(value as Instant)
        Order::totalMinorUnit.name -> order.totalMinorUnit >= value as Long
        else -> throw UnsupportedOperationException("Gte not implemented for field $field")
    }

    is DataFilter.Field.Lte<*> -> when (field) {
        Order::createdAt.name -> !order.createdAt.isAfter(value as Instant)
        Order::totalMinorUnit.name -> order.totalMinorUnit <= value as Long
        else -> throw UnsupportedOperationException("Lte not implemented for field $field")
    }

    else -> throw UnsupportedOperationException("Not implemented for ${this::class.simpleName}")
}

@Suppress("UNCHECKED_CAST")
private fun Sort.field(order: Order): Comparable<Any> = when (field) {
    Order::createdAt.name -> order.createdAt as Comparable<Any>
    Order::totalMinorUnit.name -> order.totalMinorUnit as Comparable<Any>
    Order::status.name -> order.status as Comparable<Any>
    else -> throw IllegalArgumentException("Unsupported order: $order")
}

private fun randomOrders(): List<Order> = (1..1235).map {
    Order(
        status = OrderStatus.entries.random(),
        totalMinorUnit = Random.nextLong(1000, 150000),
        createdAt = Instant.now().minusDays(Random.nextInt(10, 100)).plusSeconds(60 * Random.nextLong(10, 2000))
    )
}
