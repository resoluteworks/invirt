package invirt.mongodb

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MongoCollection(val name: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Indexed(
    val order: Order = Order.ASC,
    val caseInsensitive: Boolean = true,
    vararg val fields: String
) {

    enum class Order {
        ASC,
        DESC
    }
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TextIndexed(
    vararg val fields: String
)
