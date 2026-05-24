---
sidebar_position: 1
---

# Overview

`invirt-utils` is a small library of zero-dependency Kotlin utilities used across the rest of Invirt
and available for use in applications.

```kotlin
implementation(platform("dev.invirt:invirt-bom:${invirtVersion}"))
implementation("dev.invirt:invirt-utils")
```

The library is loosely organised into the following groups:

* [Strings](/docs/api/invirt-utils/strings) &mdash; reading time, anonymisation, case conversion, URL handling.
* [Date and time](/docs/api/invirt-utils/datetime) &mdash; day-of-month suffix, human-readable durations.
* [Currency](/docs/api/invirt-utils/currency) &mdash; minor-unit formatting using `java.util.Currency`.
* [IDs](/docs/api/invirt-utils/ids) &mdash; UUIDv7 and Base32 encodings.
* [Enums](/docs/api/invirt-utils/enums) &mdash; null-safe parsing and CSV decoding.
* [Files](/docs/api/invirt-utils/files) &mdash; working / temp directory helpers and a managed `TempDir`.
* [Threads](/docs/api/invirt-utils/threads) &mdash; `ThreadLocal.withValue` and `ThreadPool`.
* [Resources](/docs/api/invirt-utils/resources) &mdash; classpath resources as strings, properties or lists.
