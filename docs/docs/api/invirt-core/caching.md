---
sidebar_position: 10
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Caching

Thin wrappers over http4k's `CachingFilters.CacheResponse.MaxAge`, primarily for
[static assets](/docs/framework/static-assets).

### cacheDays
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    "/static/${assetsVersion}" bind cacheDays(365).then(staticAssets(devMode))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun cacheDays(days: Int): Filter
    ```
  </TabItem>
</Tabs>

### cacheOneYear
Equivalent to `cacheDays(365)`.
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun cacheOneYear(): Filter
    ```
  </TabItem>
</Tabs>
