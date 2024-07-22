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
    is DataFilter.Field<*> -> this.fieldMatches(order)
    is DataFilter.Compound -> if (this.operator == DataFilter.Compound.Operator.AND) {
        this.subFilters.all { it.matches(order) }
    } else {
        this.subFilters.any { it.matches(order) }
    }
}

private fun DataFilter.Field<*>.fieldMatches(order: Order): Boolean {
    when (this.field) {
        Order::createdAt.name -> return when (operation) {
            DataFilter.Field.Operation.GTE -> order.createdAt.isAfter(value as Instant)
            DataFilter.Field.Operation.LTE -> order.createdAt.isBefore(value as Instant)
            else -> throw UnsupportedOperationException("Not implemented for $operation")
        }

        Order::totalMinorUnit.name -> return when (operation) {
            DataFilter.Field.Operation.GTE -> order.totalMinorUnit >= value as Long
            DataFilter.Field.Operation.LTE -> order.totalMinorUnit <= value as Long
            DataFilter.Field.Operation.GT -> order.totalMinorUnit > value as Long
            DataFilter.Field.Operation.LT -> order.totalMinorUnit < value as Long
            else -> throw UnsupportedOperationException("Not implemented for $operation")
        }

        Order::status.name -> return order.status == value as OrderStatus

        else -> throw UnsupportedOperationException("Not implemented for field ${this.field}")
    }
}

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
