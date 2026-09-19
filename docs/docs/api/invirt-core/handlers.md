---
sidebar_position: 13
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Handlers

Ready-to-use routing handlers for common needs.

### HealthCheck.json
A `/health` route returning a JSON `{"status":"healthy"}` body. Intended for load balancer and
container orchestrator health probes.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val appHandler = routes(
        HealthCheck.json(),
        // ...
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    object HealthCheck {
        fun json(): RoutingHttpHandler
    }
    ```
  </TabItem>
</Tabs>

### seoRoutes
The two files a crawler reads before anything else on the site: `/sitemap.xml`, built from the entries the
application supplies, and `/robots.txt`, which points back at it with the base URL of the deployment
answering the request. `indexingEnabled` is the gate that keeps a non-production environment out of search
results - with it off, `robots.txt` disallows the whole site, `sitemap.xml` is a 404 and `entries` is never
called. Both files carry `cacheDuration` as a `Cache-Control` max-age, and the paths are available as
`SeoPaths.SITEMAP` and `SeoPaths.ROBOTS` for a filter that has to let crawlers through.

`entries` is invoked per request, so an application reading a database to build the list is the one that
decides how often that happens. The sitemap is a single `urlset`: there is no sitemap index, so a site past
the sitemaps.org limits (50,000 URLs or 50MB uncompressed) needs its own handler, and `changefreq` and
`priority` are not emitted.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val appHandler = routes(
        seoRoutes(
            baseUrl = config.baseUrl,
            indexingEnabled = config.seoIndexingEnabled,
            cacheDuration = Duration.ofHours(1)
        ) {
            listOf(SitemapEntry("${config.baseUrl}/")) +
                articleService.published().map { SitemapEntry("${config.baseUrl}/articles/${it.id}", it.updatedAt) }
        },
        // ...
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun seoRoutes(
        baseUrl: String,
        indexingEnabled: Boolean,
        cacheDuration: Duration,
        entries: () -> List<SitemapEntry>
    ): RoutingHttpHandler

    data class SitemapEntry(val loc: String, val lastMod: Instant? = null)

    object SeoPaths {
        const val SITEMAP = "/sitemap.xml"
        const val ROBOTS = "/robots.txt"
    }
    ```
  </TabItem>
</Tabs>

### staticAssets
A routing handler that serves static assets either from the filesystem (when `developmentMode = true`)
or from the classpath. See [Static assets](/docs/framework/static-assets) for the full setup.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun staticAssets(
        developmentMode: Boolean,
        classpathLocation: String = "webapp/static",
        directory: String = "src/main/resources/webapp/static"
    ): RoutingHttpHandler
    ```
  </TabItem>
</Tabs>
