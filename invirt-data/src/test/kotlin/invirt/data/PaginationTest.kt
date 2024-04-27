package invirt.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class PaginationTest : StringSpec({

    "basic paging" {
        Pagination(Page(0, 10), 10).assertIs(listOf(0))
            .assertCurrentPageIndex(0)
            .assertHasPrev(false)
            .assertHasNext(false)
            .assertPrevIndex(null)
            .assertPrev(null)
            .assertNextIndex(null)
            .assertNext(null)

        Pagination(Page(0, 10), 20).assertIs(listOf(0, 1))
            .assertCurrentPageIndex(0)
            .assertHasPrev(false)
            .assertHasNext(true)
            .assertPrevIndex(null)
            .assertPrev(null)
            .assertNextIndex(1)
            .assertNext(Page(10, 10))

        Pagination(Page(10, 10), 20).assertIs(listOf(0, 1))
            .assertCurrentPageIndex(1)
            .assertHasPrev(true)
            .assertPrev(Page(0, 10))
            .assertHasNext(false)
            .assertNext(null)

        Pagination(Page(0, 5), 20).assertIs(listOf(0, 1, 2, 3))
            .assertCurrentPageIndex(0)
            .assertHasPrev(false)
            .assertPrev(null)
            .assertHasNext(true)
            .assertNext(Page(5, 5))

        Pagination(Page(5, 5), 20).assertIs(listOf(0, 1, 2, 3))
            .assertCurrentPageIndex(1)
            .assertHasPrev(true)
            .assertHasNext(true)
            .assertPrev(Page(0, 5))
            .assertNext(Page(10, 5))

        Pagination(Page(20, 10), 23).assertIs(listOf(0, 1, 2))
            .assertCurrentPageIndex(2)
            .assertHasPrev(true)
            .assertPrev(Page(10, 10))
            .assertHasNext(false)
            .assertNext(null)

        Pagination(Page(10, 10), 100).assertIs((0..9).toList())
        Pagination(Page(10, 10), 90).assertIs((0..8).toList())
        Pagination(Page(10, 10), 70).assertIs((0..6).toList())

        Pagination(Page(60, 10), 70).assertIs((0..6).toList())
            .assertCurrentPageIndex(6)
            .assertHasPrev(true)
            .assertPrevIndex(5)
            .assertPrev(Page(50, 10))
            .assertHasNext(false)
            .assertNextIndex(null)
            .assertNext(null)
    }

    "complex paging" {
        Pagination(Page(10, 10), 1000).assertIs(listOf(0, 1, 2, 3, 4, null, 99))
            .assertCurrentPageIndex(1)
            .assertHasPrev(true)
            .assertPrev(Page(0, 10))
            .assertHasNext(true)
            .assertNext(Page(20, 10))

        Pagination(Page(20, 10), 1000).assertIs(listOf(0, 1, 2, 3, 4, null, 99))
            .assertCurrentPageIndex(2)
            .assertHasPrev(true)
            .assertPrev(Page(10, 10))
            .assertHasNext(true)
            .assertNext(Page(30, 10))

        Pagination(Page(40, 10), 1000).assertIs(listOf(0, null, 3, 4, 5, null, 99))
        Pagination(Page(720, 10), 1000).assertIs(listOf(0, null, 71, 72, 73, null, 99))
        Pagination(Page(720, 10), 995).assertIs(listOf(0, null, 71, 72, 73, null, 99))

        // Last pages
        Pagination(Page(940, 10), 1000).assertIs(listOf(0, null, 93, 94, 95, null, 99))
        Pagination(Page(950, 10), 1000).assertIs(listOf(0, null, 94, 95, 96, null, 99))
        Pagination(Page(960, 10), 1000).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
        Pagination(Page(970, 10), 1000).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
        Pagination(Page(980, 10), 1000).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
        Pagination(Page(990, 10), 1000).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
    }

    "getPage" {
        val pagination = Pagination(Page(0, 10), 100)
        pagination.getPage(0) shouldBe Page(0, 10)
        pagination.getPage(8) shouldBe Page(80, 10)
        pagination.getPage(9) shouldBe Page(90, 10)
        pagination.getPage(10) shouldBe null
    }

    "totalPages" {
        Pagination(Page(0, 10), 100).totalPages shouldBe 10
        Pagination(Page(0, 10), 93).totalPages shouldBe 10
    }
})

fun Pagination.assertIs(list: List<Int?>): Pagination {
    this.pageIndices shouldContainExactly list
    return this
}

fun Pagination.assertCurrentPageIndex(currentPageNumber: Int): Pagination {
    this.currentPageIndex shouldBe currentPageNumber
    return this
}

fun Pagination.assertHasPrev(value: Boolean): Pagination {
    this.hasPrev shouldBe value
    return this
}

fun Pagination.assertHasNext(value: Boolean): Pagination {
    this.hasNext shouldBe value
    return this
}

fun Pagination.assertPrevIndex(page: Int?): Pagination {
    this.prevPageIndex shouldBe page
    return this
}

fun Pagination.assertPrev(page: Page?): Pagination {
    this.prevPage shouldBe page
    return this
}

fun Pagination.assertNextIndex(page: Int?): Pagination {
    this.nextPageIndex shouldBe page
    return this
}

fun Pagination.assertNext(page: Page?): Pagination {
    this.nextPage shouldBe page
    return this
}
