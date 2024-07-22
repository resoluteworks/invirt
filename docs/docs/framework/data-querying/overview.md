---
sidebar_position: 1
---

# Overview

:::note [Example application](https://github.com/resoluteworks/invirt/tree/main/examples/data-querying)
:::

Invirt provides the wiring for an application to derive filtering, pagination and sorting logic
from a request's query parameters. A set of abstractions for these components are define
by invirt-data, a small library that you can add to your application to leverage these
capabilities.

## Dependency
```kotlin
implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
implementation("io.resoluteworks:invirt-data")
```

## Rationale
It's important to note that Invirt doesn't provide implementations of [these abstractions](/docs/api/invirt-data/data-filter)
for specific databases, and that would be out of scope for the foreseeable future.

The main reason for this is because we wanted to provide greater flexibility of query building,
and a decoupling of DB querying logic from the presentation layer. Mapping URL query parameters
directly to database fields, is not always the right approach to achieve that.

In some cases, the absence of a query parameter means the system would apply an implicit filter.
For example, a website listing properties for sale, by default would only list
properties that are still available: `sold == false`. It would offer the user an option to include sold ones
via `&include-sold=true`, but the presence of that query parameter would imply the _absence_ of the
`sold == false` DB filter.

Another example are activities that have a start date and a deadline, like
applying to take part in a competition. A user-friendly filter here might be "Show open competitions", indicating
events that have officially open the application process, but haven't reached the deadline date yet (they're still
taking applications). In this case, the system might want to provide a compound filter for a `&status=open` query parameter:
```sql
(openingDate >= TODAY) AND (TODAY < deadline)
```

While all of these have workarounds and alternatives, all of them invariably end up coupling the user interface
to the underlying model of the database. It's this separation of concerns that we aim to solve with Invirt and the
abstractions we've defined.
