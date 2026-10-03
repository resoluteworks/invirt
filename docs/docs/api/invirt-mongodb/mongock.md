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

## Change unit discovery
`runMigrations` finds a package's change units with the same classpath scan Mongock runs for a scan
package (types in the package and its subpackages annotated with `@ChangeUnit` or the legacy
`@ChangeLog`, plus their subtypes), and hands them to Mongock as explicit classes. The scan is kept per
package and reused by later calls through the same context classloader, so a process that runs the
same migrations many times, such as a test suite that boots an application per spec, scans once.
Mongock still orders change units by their `order` and executes and records them as usual.

Mongock scans the package itself instead when the package has no change units, or when the context
classloader cannot resolve the scanned classes (Mongock resolves explicit classes by name through it,
and would otherwise skip them without an error).

Each run logs `Ran MongoDB migrations` with `packageName`, `changeUnits` (the number found),
`discovery` (`scanned`, `cached` or `package-scan`), `discoveryMs` and `durationMs` (the Mongock run).
`runMigration` logs the same message with `migrationClass` and `durationMs`.

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
        mongo.database.getCollection<Product>("products").createIndexes(
            listOf(
                IndexModel(Indexes.ascending(Product::name.name)),
                *TimestampedDocument.allIndices()
            )
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
