package invirt.data

import kotlin.reflect.KProperty

data class Sort(
    val field: String,
    val order: SortOrder
) {

    fun revert(): Sort = Sort(field, order.revert())

    override fun toString(): String = "$field:${order.name.lowercase()}"

    companion object {
        operator fun invoke(sortString: String): Sort {
            val elements = sortString.split(":")
            if (elements.size != 2 || elements.any { it.isBlank() }) {
                throw IllegalArgumentException("Invalid sort string $sortString")
            }
            val order = SortOrder.fromString(elements[1])
            return Sort(elements[0], order)
        }

        fun asc(field: String) = Sort(field, SortOrder.ASC)
        fun desc(field: String) = Sort(field, SortOrder.DESC)
    }
}

enum class SortOrder {
    ASC,
    DESC;

    fun revert(): SortOrder = if (this == ASC) DESC else ASC

    companion object {
        fun fromString(orderString: String): SortOrder = valueOf(orderString.uppercase())
    }
}

fun KProperty<*>.sortAsc(): Sort = Sort(this.name, SortOrder.ASC)

fun String.sortAsc(): Sort = Sort(this, SortOrder.ASC)

fun KProperty<*>.sortDesc(): Sort = Sort(this.name, SortOrder.DESC)

fun String.sortDesc(): Sort = Sort(this, SortOrder.DESC)
