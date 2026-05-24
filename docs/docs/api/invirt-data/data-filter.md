---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# DataFilter

`DataFilter` is a `sealed interface` describing data-querying logic independently from any specific
database technology. The rationale is discussed [here](/docs/framework/data-querying/overview#rationale).

A `DataFilter` is either a `Field` operation, or a logical combination (`And` / `Or`) of other filters.
Implementations like [`invirt-mongodb`](/docs/api/invirt-mongodb/filters) translate a `DataFilter` into
native database constructs (e.g. `DataFilter.mongoFilter()`).

```kotlin
sealed interface DataFilter {
    sealed class Field : DataFilter
    data class Or(val filters: List<DataFilter>) : DataFilter
    data class And(val filters: List<DataFilter>) : DataFilter
}
```

## DataFilter.Field

The available field operations are:

| Operation | Example |
|---|---|
| `Eq<Value>(field, value)` | `User::name eq "John"` |
| `Ne<Value>(field, value)` | `User::status ne Status.DELETED` |
| `Gt<Value>(field, value)` | `User::age gt 18` |
| `Gte<Value>(field, value)` | `User::age gte 18` |
| `Lt<Value>(field, value)` | `Product::priceMinor lt 1000_00` |
| `Lte<Value>(field, value)` | `Product::priceMinor lte 1000_00` |
| `Exists(field)` | `User::email.exists()` |
| `DoesntExist(field)` | `User::email.doesntExist()` |
| `WithinGeoBounds(field, GeoBoundingBox)` | `User::location withinGeoBounds bbox` |

Filters can be constructed either by string field name or via `KProperty` references, using the
companion factory methods on `DataFilter` or the infix/extension shortcuts:

```kotlin
// Companion factories
DataFilter.eq("name", "John")
DataFilter.lt("priceMinor", 1000_00)

// String extensions
"status" eq "PUBLISHED"
"priceMinor" gte 100

// KProperty extensions
Product::priceMinor lte 1000_00
User::email.exists()
```

## DataFilter.And / DataFilter.Or

Logical combinations of filters. The framework provides factory functions and a `flatten()` helper that
collapses single-element `And` / `Or` wrappers.

```kotlin
val filter = orDataFilter(
    Product::status ne Status.PUBLISHED,
    andDataFilter(
        Product::status eq Status.PUBLISHED,
        Product::priceMinor gte 200_000
    )
)

// Same logical filter without redundant single-element wrappers
val simplified = filter.flatten()
```

In an Invirt application the most common way to build a filter is from
[query parameters](/docs/framework/data-querying/example#filtering-logic) via `queryDataFilter`, where
the individual field filters are returned by lambdas and combined according to the
[`QueryDataFilter.Operator`](/docs/framework/data-querying/example#filtering-logic).
