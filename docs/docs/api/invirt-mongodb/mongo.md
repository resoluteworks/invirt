---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Mongo

A thin wrapper around `MongoClient` and `MongoDatabase` from the
[MongoDB Kotlin driver](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/). The database
name is parsed from the connection string &mdash; an explicit database name in the URI is required.

```kotlin
class Mongo(val connectionString: String) {
    val databaseName: String
    val database: MongoDatabase
    fun <Result> runInTransaction(block: (ClientSession) -> Result): Result
    fun close()
}
```

### Construction
```kotlin
val mongo = Mongo("mongodb://localhost:27017/myapp")
mongo.databaseName  // "myapp"
mongo.database      // com.mongodb.kotlin.client.MongoDatabase
```

The first access to `database` pings the server and logs success. Without a database segment in the
URI the constructor throws `IllegalArgumentException`.

### Transactions
`runInTransaction` opens a session with `WriteConcern.MAJORITY`, executes the block and commits.
On exception the transaction is aborted and the exception is rethrown.

```kotlin
val updated = mongo.runInTransaction { session ->
    productCollection.txUpdate(session, product)
    auditCollection.txInsert(session, AuditEntry(...))
    product
}
```

### Client session interop
`invirt-mongodb` exposes a `JavaClientSession` typealias for `com.mongodb.client.ClientSession` and a
`JavaClientSession.kotlin()` extension to convert it into the Kotlin driver's session type. This is
used internally by the [Mongock migrations](/docs/api/invirt-mongodb/mongock) helpers.
