package invirt.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength

fun caseInsensitive(
    locale: String = "en",
    strength: CollationStrength = CollationStrength.TERTIARY
): Collation = Collation.builder()
    .locale(locale)
    .collationStrength(strength)
    .caseLevel(false)
    .build()
