package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.kotlin.client.FindIterable

fun <T : Any> FindIterable<T>.caseInsensitive(
    locale: String = "en",
    strength: CollationStrength = CollationStrength.TERTIARY
): FindIterable<T> {
    val collation = Collation.builder()
        .locale(locale)
        .collationStrength(strength)
        .caseLevel(false)
        .build()
    return this.collation(collation)
}
