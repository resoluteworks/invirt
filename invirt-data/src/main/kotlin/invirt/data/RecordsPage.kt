package invirt.data

data class RecordsPage<T : Any>(
    val records: List<T>,
    val count: Long,
    val page: Page,
    val sort: List<Sort> = emptyList()
) {
    val pagination = Pagination(page, count)

    companion object {
        fun <T : Any> empty() = RecordsPage<T>(emptyList(), 0, Page(0, 1))
    }
}
