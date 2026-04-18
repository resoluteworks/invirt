package invirt.mongodb.cursor

import invirt.data.sortAsc
import invirt.data.sortDesc
import invirt.mongodb.mongoNow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class CursorTest : FreeSpec() {

    init {
        "token and fromToken should serialize and deserialize consistently" {
            val cursor = Cursor(
                sort = listOf("submittedAt".sortDesc(), "id".sortAsc()),
                values = listOf(mongoNow(), "123").map { CursorValue.of(it) },
                direction = CursorDirection.FORWARD
            )

            Cursor.fromToken(cursor.token) shouldBe cursor
        }
    }
}
