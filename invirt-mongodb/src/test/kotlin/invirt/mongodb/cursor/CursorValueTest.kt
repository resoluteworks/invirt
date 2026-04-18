package invirt.mongodb.cursor

import invirt.mongodb.mongoNow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class CursorValueTest : FreeSpec() {

    init {
        "should encode and decode correctly" - {
            CursorValue.Type.LOCAL_DATE.name {
                CursorValue.of(LocalDate.of(2024, 6, 1)).decode() shouldBe LocalDate.of(2024, 6, 1)
            }

            CursorValue.Type.INT.name {
                CursorValue.of(Int.MIN_VALUE).decode() shouldBe Int.MIN_VALUE
            }

            CursorValue.Type.LONG.name {
                CursorValue.of(Long.MIN_VALUE).decode() shouldBe Long.MIN_VALUE
            }

            CursorValue.Type.DOUBLE.name {
                CursorValue.of(Double.MIN_VALUE).decode() shouldBe Double.MIN_VALUE
            }

            CursorValue.Type.STRING.name {
                CursorValue.of("test string").decode() shouldBe "test string"
            }

            CursorValue.Type.INSTANT.name {
                val now = mongoNow()
                CursorValue.of(now).decode() shouldBe now
            }
        }
    }
}
