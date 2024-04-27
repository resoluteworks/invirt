package invirt.data

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SortTest : StringSpec({

    "from sortString" {
        Sort("name:Asc") shouldBe Sort("name", SortOrder.ASC)
        Sort("firstName:DesC") shouldBe Sort("firstName", SortOrder.DESC)
        shouldThrowWithMessage<IllegalArgumentException>("Invalid sort string firstNameDesC") {
            Sort("firstNameDesC")
        }
        shouldThrowWithMessage<IllegalArgumentException>("Invalid sort string firstName:asc:desc") {
            Sort("firstName:asc:desc")
        }
        shouldThrowWithMessage<IllegalArgumentException>("Invalid sort order EITHER") {
            Sort("name:either")
        }
    }

    "toString" {
        Sort("name", SortOrder.ASC).toString() shouldBe "name:ASC"
        Sort("lastName", SortOrder.DESC).toString() shouldBe "lastName:DESC"
    }

    "sort order revert" {
        SortOrder.ASC.revert() shouldBe SortOrder.DESC
        SortOrder.DESC.revert() shouldBe SortOrder.ASC
    }

    "sort revert" {
        Sort("name", SortOrder.ASC).revert() shouldBe Sort("name", SortOrder.DESC)
        Sort("name", SortOrder.DESC).revert() shouldBe Sort("name", SortOrder.ASC)
    }

    "property sort" {
        data class Pojo(
            val name: String
        )
        Pojo::name.sortAsc() shouldBe Sort("name", SortOrder.ASC)
        Pojo::name.sortDesc() shouldBe Sort("name", SortOrder.DESC)
    }

    "Sort.asc/Sort.desc" {
        Sort.asc("name") shouldBe Sort("name", SortOrder.ASC)
        Sort.desc("name") shouldBe Sort("name", SortOrder.DESC)
    }
})
