package invirt.core.handlers

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus
import java.time.Duration
import java.time.Instant

class SeoTest : StringSpec({

    val defaultEntries = listOf(
        SitemapEntry("https://example.com/"),
        SitemapEntry("https://example.com/items/1", Instant.parse("2026-09-01T10:15:30Z"))
    )

    fun routes(
        indexingEnabled: Boolean = true,
        baseUrl: String = "https://example.com",
        cacheDuration: Duration = Duration.ofHours(1),
        entries: () -> List<SitemapEntry> = { defaultEntries }
    ) = seoRoutes(baseUrl, indexingEnabled, cacheDuration, entries)

    "sitemap lists every entry, with a lastmod only where there is one" {
        val response = routes()(Request(Method.GET, SeoPaths.SITEMAP))

        response shouldHaveStatus Status.OK
        response.header("Content-Type") shouldBe "application/xml; charset=utf-8"
        response.bodyString() shouldBe """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
              <url><loc>https://example.com/</loc></url>
              <url><loc>https://example.com/items/1</loc><lastmod>2026-09-01T10:15:30Z</lastmod></url>
            </urlset>
        """.trimIndent()
    }

    "an empty sitemap is still a valid urlset" {
        val response = routes(entries = { emptyList() })(Request(Method.GET, SeoPaths.SITEMAP))

        response shouldHaveStatus Status.OK
        response.bodyString() shouldBe """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            </urlset>
        """.trimIndent()
    }

    // A loc is element text, so an unescaped ampersand from a query string would break the document.
    "locs are XML escaped" {
        val response = routes(
            entries = { listOf(SitemapEntry("""https://example.com/search?q=a&b<c>"d'e""")) }
        )(Request(Method.GET, SeoPaths.SITEMAP))

        response.bodyString() shouldContain
            "<loc>https://example.com/search?q=a&amp;b&lt;c&gt;&quot;d&apos;e</loc>"
    }

    "robots points at the sitemap of the deployment answering the request" {
        val response = routes(baseUrl = "https://beta.example.com")(Request(Method.GET, SeoPaths.ROBOTS))

        response shouldHaveStatus Status.OK
        response.header("Content-Type") shouldBe "text/plain; charset=utf-8"
        response.bodyString() shouldBe "User-agent: *\nSitemap: https://beta.example.com/sitemap.xml\n"
    }

    "a base URL with a trailing slash does not double up" {
        val response = routes(baseUrl = "https://example.com/")(Request(Method.GET, SeoPaths.ROBOTS))

        response.bodyString() shouldBe "User-agent: *\nSitemap: https://example.com/sitemap.xml\n"
    }

    "both files carry the cache duration as a max-age" {
        val handler = routes(cacheDuration = Duration.ofMinutes(30))

        handler(Request(Method.GET, SeoPaths.SITEMAP)).header("Cache-Control") shouldContain "max-age=1800"
        handler(Request(Method.GET, SeoPaths.ROBOTS)).header("Cache-Control") shouldContain "max-age=1800"
    }

    "indexing disabled disallows everything and hides the sitemap" {
        var built = 0
        val handler = routes(
            indexingEnabled = false,
            entries = {
                built++
                defaultEntries
            }
        )

        val robots = handler(Request(Method.GET, SeoPaths.ROBOTS))
        robots shouldHaveStatus Status.OK
        robots.bodyString() shouldBe "User-agent: *\nDisallow: /\n"
        robots.header("Cache-Control") shouldContain "max-age=3600"

        val sitemap = handler(Request(Method.GET, SeoPaths.SITEMAP))
        sitemap shouldHaveStatus Status.NOT_FOUND
        sitemap.bodyString() shouldBe ""

        // Nothing is enumerated while indexing is off, so the application pays nothing for the routes.
        built shouldBe 0
    }

    // The entries are read per request; memoising them is the application's decision, not the library's.
    "entries are read on every sitemap request" {
        var built = 0
        val handler = routes(
            entries = {
                built++
                defaultEntries
            }
        )

        handler(Request(Method.GET, SeoPaths.SITEMAP))
        handler(Request(Method.GET, SeoPaths.SITEMAP))

        built shouldBe 2
    }

    "nothing else is bound" {
        routes()(Request(Method.GET, "/other")) shouldHaveStatus Status.NOT_FOUND
    }
})
