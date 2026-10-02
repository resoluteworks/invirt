package invirt.mongodb.mongock

import invirt.mongodb.Mongo
import invirt.mongodb.mongock.migrations.Company
import org.bson.Document

const val DATA_PACKAGE = "invirt.mongodb.mongock.migrations.data"
const val ORDERED_PACKAGE = "invirt.mongodb.mongock.migrations.ordered"
const val SCAN_PACKAGE = "invirt.mongodb.mongock.migrations.scan"
const val PACKAGE_WITHOUT_CHANGE_UNITS = "invirt.mongodb.mongock.migrations.none"

/**
 * Runs [block] with [classLoader] as the calling thread's context classloader, and restores the previous one after.
 */
inline fun <T> withContextClassLoader(classLoader: ClassLoader?, block: () -> T): T {
    val thread = Thread.currentThread()
    val previous = thread.contextClassLoader
    thread.contextClassLoader = classLoader
    try {
        return block()
    } finally {
        thread.contextClassLoader = previous
    }
}

fun Mongo.companyNames(): Set<String> =
    database.getCollection<Company>(Company.COLLECTION).find().toList().map { it.name }.toSet()

/**
 * The `changeId`, `type` and `state` of each entry Mongock recorded, in the order it recorded them.
 */
fun Mongo.mongockChangeLog(): List<Triple<String, String, String>> =
    database.getCollection<Document>("mongockChangeLog").find().sort(Document("_id", 1)).toList()
        .map { Triple(it.getString("changeId"), it.getString("type"), it.getString("state")) }
