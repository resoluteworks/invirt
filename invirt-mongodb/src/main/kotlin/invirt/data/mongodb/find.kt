package invirt.data.mongodb

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.kotlin.client.FindIterable

fun <T : Any> FindIterable<T>.caseInsensitive(): FindIterable<T> {
    return this.collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
}
