---
sidebar_position: 3
---

# Static resources

Static resources in an Invirt web application are largely managed by underlying http4k components, with
a few convenience components on top. Invirt provides out of the box a mechanism to handle versioning
of static resources, and simplify caching.

By default, resources are mapped to `classpath:/webapp/static` or the `src/main/resources/webapp/static`
local directoy, depending on hot reload settings (production vs local development).

```kotlin
// Routes /webapp/static/v1 to classpath:/webapp/static
val handler = StaticResources.Classpath("v1", "webapp/static")

// Routes /webapp/static/v2 to the src/main/resources/webapp/static directory
val handler = StaticResources.HotReload("v2", "src/main/resources/webapp/static")

// [RECOMMENDED]
// Routes /webapp/static/<gitCommitId> to
//   - classpath:/webapp/static when hotReload is false
//   - src/main/resources/webapp/static local directory when hotReload is true
val handler = StaticResources(hotReload = developmentModeEnabled, version = gitCommitId)
```
