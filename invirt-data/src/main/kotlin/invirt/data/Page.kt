package invirt.data

data class Page(
    val from: Int,
    val size: Int
) {

    /**
     * Returns the index of the current page relative to the beginning of the
     * pagination (from = 0).
     */
    val pageIndex: Int = if (size > 0L) {
        from / size
    } else {
        throw IllegalArgumentException("Page size must be greater than 0")
    }

    init {
        if (from % size != 0) {
            throw IllegalArgumentException("from must be a multiple of size or 0")
        }
    }
}

fun <T> List<T>.page(page: Page): List<T> = subList(page.from, (page.from + page.size).coerceAtMost(size))
