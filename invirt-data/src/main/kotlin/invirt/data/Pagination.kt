package invirt.data

/**
 * This is an (opinionated) implementation of a helper component for displaying pagination controls in web applications.
 * For long sets of pages, the algorithm is designed to compress the display in order to maintain a reasonable
 * size for the pagination control.
 *
 * For example, when page 12 is selected in a set of 100 pages, we'd have something like this: [1 ... 11 <12> 13 ... 100]
 * (<x> indicates that x the currently selected page)
 *
 * @pageNumbers returns the list of page numbers that should be displayed, using null values for where ellipsis (...) or some other "intermediate pages" symbol
 * should be presented. Please note that these numbers are zero-indexed [0 1 2 ...] so for display purposes these should be converted to 1-indexed values [1 2 3 ...].
 *
 * When the total number of pages is <=10 the algorithm will return all the pages: [1 2 3 4 5 6 7 8 9 10]
 *
 * When the total number of pages is >10 the following rules apply:
 * (1): We always display a maximum of 7 pages, including ellipsis (null) indicators: [1 ... 12 <13> 14 ... 100].
 *
 * (2): First and last pages should always be reachable. This removes the need for <first>/<last> actions.
 *
 * (3): The current page should always have the previous and next pages displayed. For example if the current page is 12
 * then we display [1 ... 11 <12> 13 ... 100] and not [1 ... <12> 13 14 ... 100]. This removes the need for <prev>/<next> actions.
 *
 * (4): When the current page is in the first 4 pages we display all first 5 pages: [1 2 3 <4> 5 ... 100] because there's no point in displaying
 * an ellipsis (...) where there is only one page (2, in this case). This is also in line with rules (1) and (3).
 *
 * (5): When the current page is in the last 4 pages we display all last 5 pages: [1 ... 96 <97> 98 99 100] because there's no point in displaying
 * an ellipsis (...) where there is only one page (99, in this case). This is also in line with rule (1) and (3).
 */
data class Pagination(
    val currentPage: Page,
    val totalCount: Long
) {

    val totalPages = if (totalCount > 0) {
        var numberOfPages = totalCount / currentPage.size
        if (totalCount % currentPage.size > 0) {
            numberOfPages++
        }
        numberOfPages.toInt()
    } else {
        0
    }

    val pageIndices: List<Int?> = createPages()
    val hasPrev: Boolean = currentPage.pageIndex > 0
    val hasNext: Boolean = currentPage.pageIndex < totalPages - 1
    val currentPageIndex: Int = currentPage.pageIndex
    val prevPageIndex: Int? = if (hasPrev) currentPageIndex - 1 else null
    val nextPageIndex: Int? = if (hasNext) currentPageIndex + 1 else null

    val prevPage: Page? = prevPageIndex
        ?.let { Page(prevPageIndex * currentPage.size, currentPage.size) }

    val nextPage: Page? = nextPageIndex
        ?.let { Page(nextPageIndex * currentPage.size, currentPage.size) }

    fun getPage(index: Int): Page? {
        return if (index < totalPages) {
            val from = index * currentPage.size
            Page(from, currentPage.size)
        } else {
            null
        }
    }

    private fun createPages(): List<Int?> {
        val currentPage = currentPage.pageIndex

        // If at most 10 pages then display all of them
        if (totalPages <= 10) {
            return (0 until totalPages).toList()
        }

        // Otherwise display "..." (null) for intermediate pages
        return when {
            // Selected page is from 1 to 4
            currentPage <= 3 -> {
                listOf(0, 1, 2, 3, 4, null, totalPages - 1)
            }

            // Selected page is in the last 4 pages
            currentPage >= totalPages - 4 -> {
                listOf(0, null, totalPages - 5, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1)
            }

            // Selected page is in the middle somewhere, display first, last and adjacent pages
            else -> {
                return listOf(0, null, currentPage - 1, currentPage, currentPage + 1, null, totalPages - 1)
            }
        }
    }
}
