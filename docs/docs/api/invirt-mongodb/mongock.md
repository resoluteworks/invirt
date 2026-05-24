---
sidebar_position: 10
---

# Mongock migrations

Helpers for running [Mongock](https://www.mongock.io/) migrations using the same `MongoClient` as the
rest of the application, so migrations share transactional state with the surrounding code.

```kotlin
fun Mongo.runMigrations(packageName: String, vararg dependencies: Any)
fun Mongo.runMigration(migrationClass: KClass<*>, vararg dependencies: Any)
```

### Example
```kotlin
mongo.runMigrations(
    packageName = "myapp.persistence.migrations",
    productService,
    auditService
)
```

`dependencies` are registered with the Mongock runner and can be injected into migration classes.

## Migration interfaces
Three convenience interfaces for the most common migration shapes. Implementations are picked up by
package or class scan.

### ModelMigration
For schema-style changes that cannot run inside a transaction (e.g. index creation). The
`@BeforeExecution` hook is wired by these interfaces so you only implement `model(...)` and
`rollbackModel(...)`.

```kotlin
class CreateProductIndexes : ModelMigration {
    override fun model(mongo: Mongo) {
        mongo.database.getCollection<Product>("products").createIndices(
            Product::name.asc(),
            *TimestampedDocument.allIndices()
        )
    }
}
```

### DataMigration
For data changes that run inside a transaction.

```kotlin
class FixProductPrices : DataMigration {
    override fun data(mongo: Mongo, javaSession: JavaClientSession) {
        // operate via javaSession.kotlin() to use the Kotlin driver
    }
    override fun rollbackData(mongo: Mongo, javaSession: JavaClientSession) { /* ... */ }
}
```

### ModelAndDataMigration
Combines both phases for migrations that need to alter the schema and then move data accordingly.
