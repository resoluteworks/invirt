package invirt.data

data class RecordsPage<T : Any>(
    val records: List<T>,
    val totalCount: Long,
    val page: Page
) {
    val pagination = Pagination(page, totalCount)

    fun <R : Any> map(map: (T) -> R): RecordsPage<R> = RecordsPage(records = records.map(map), totalCount, page)

    companion object {
        fun <T : Any> empty() = RecordsPage<T>(emptyList(), 0, Page(0, 1))
    }
}
