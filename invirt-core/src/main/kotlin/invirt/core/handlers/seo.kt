package invirt.core.handlers

import invirt.core.GET
import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.CachingFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes
import java.time.Duration
import java.time.Instant

/**
 * The paths [seoRoutes] binds. A filter that has to let crawlers through before anything else in the chain
 * (an onboarding gate, a sign-in redirect) matches on these rather than on its own string literals.
 */
object SeoPaths {
    const val SITEMAP = "/sitemap.xml"
    const val ROBOTS = "/robots.txt"
}

/**
 * One `<url>` in the sitemap: [loc] is the absolute URL of the page, and [lastMod] the moment it last
 * changed, omitted when the page has nothing that carries a modification time.
 */
data class SitemapEntry(val loc: String, val lastMod: Instant? = null)

/**
 * The two files a crawler reads before anything else on the site. `/sitemap.xml` lists the [entries] the
 * application supplies, and `/robots.txt` points back at it with [baseUrl], so it is served here rather
 * than as a static file: the `Sitemap:` line has to carry the URL of the deployment answering the request,
 * which differs per environment.
 *
 * [indexingEnabled] is the gate that keeps a non-production environment out of search results without
 * changing the routes production serves: with it off, `/robots.txt` disallows the whole site and
 * `/sitemap.xml` is a 404, and [entries] is never called.
 *
 * [entries] is invoked per request, so an application that reads a database to build the list is the one
 * that decides how often that happens - wrap it in whatever memoisation it wants. Both files also carry a
 * [cacheDuration] `Cache-Control` max-age for any cache sitting between the application and a crawler,
 * which a crawler's own re-fetch schedule ignores.
 *
 * The sitemap is a single `urlset` document: there is no sitemap index, so a site past the sitemaps.org
 * limits (50,000 URLs or 50MB uncompressed) needs its own handler, and `changefreq` and `priority` are not
 * emitted - Google ignores both.
 */
fun seoRoutes(
    baseUrl: String,
    indexingEnabled: Boolean,
    cacheDuration: Duration,
    entries: () -> List<SitemapEntry>
): RoutingHttpHandler {
    val sitemapUrl = "${baseUrl.trimEnd('/')}${SeoPaths.SITEMAP}"
    return CachingFilters.CacheResponse.MaxAge(cacheDuration).then(
        if (indexingEnabled) {
            routes(
                SeoPaths.SITEMAP GET {
                    Response(Status.OK)
                        .header("Content-Type", ContentType.APPLICATION_XML.toHeaderValue())
                        .body(sitemapXml(entries()))
                },
                SeoPaths.ROBOTS GET {
                    Response(Status.OK)
                        .header("Content-Type", ContentType.TEXT_PLAIN.toHeaderValue())
                        .body("User-agent: *\nSitemap: $sitemapUrl\n")
                }
            )
        } else {
            routes(
                SeoPaths.SITEMAP GET { Response(Status.NOT_FOUND) },
                SeoPaths.ROBOTS GET {
                    Response(Status.OK)
                        .header("Content-Type", ContentType.TEXT_PLAIN.toHeaderValue())
                        .body("User-agent: *\nDisallow: /\n")
                }
            )
        }
    )
}

/**
 * A sitemaps.org `urlset` holding one `<url>` per entry, with a `<lastmod>` only for the entries that
 * carry one.
 */
private fun sitemapXml(entries: List<SitemapEntry>): String = buildString {
    appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
    entries.forEach { entry ->
        val lastMod = entry.lastMod?.let { "<lastmod>$it</lastmod>" } ?: ""
        appendLine("  <url><loc>${xmlEscape(entry.loc)}</loc>$lastMod</url>")
    }
    append("</urlset>")
}

/**
 * Escapes the characters XML gives special meaning to - `&`, `<` and `>` in any text, plus the two quote
 * characters that delimit an attribute value - for the [sitemapXml] `loc` values. The quote characters are
 * escaped defensively; a `<loc>` value is element text, where only `&`, `<` and `>` are actually required
 * to be escaped.
 */
private fun xmlEscape(value: String): String = value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")
