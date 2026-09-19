package invirt.pebble

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtPebbleConfig
import invirt.core.views.renderTemplate
import invirt.pebble.filters.pebbleFilter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.pebbletemplates.pebble.error.PebbleException
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.routes

class PebbleFiltersTest : StringSpec({

    val shoutFilter = pebbleFilter("shout", "suffix") { input ->
        input?.let { "${it.toString().uppercase()}${args["suffix"] ?: "!"}" }
    }

    val explodingFilter = pebbleFilter("explode") {
        throw PebbleException(null, "Filter [$name] always fails", lineNumber, template.name)
    }

    fun render(template: String, model: Map<String, Any?>): String {
        Invirt.configure(pebble = InvirtPebbleConfig(extensions = listOf(pebbleFilters(shoutFilter, explodingFilter))))
        val handler = routes("/test" GET { renderTemplate(it, template, model) })
        return handler(Request(Method.GET, "/test")).bodyString()
    }

    // The filtered value is the lambda's argument and the named arguments are on the receiver.
    "a filter built with pebbleFilter renders, with and without its argument" {
        render("filter-shout", mapOf("word" to "hello")).trim() shouldBe "HELLO! HELLO?!"
    }

    // Null reaches the lambda rather than short-circuiting: this filter chooses to render nothing for it,
    // while dateWithDaySuffix chooses to fail.
    "a null input reaches the filter body" {
        render("filter-shout-null", emptyMap()).trim() shouldBe "[]"
    }

    "the builder carries the filter name and the template line into a failure" {
        val exception = shouldThrow<PebbleException> {
            render("filter-explode", emptyMap())
        }

        exception.message shouldContain "Filter [explode] always fails"
        exception.message shouldContain "filter-explode"
    }

    "pebbleFilter declares its argument names" {
        shoutFilter.name shouldBe "shout"
        shoutFilter.argumentNames shouldBe listOf("suffix")
        explodingFilter.argumentNames shouldBe emptyList()
    }
})
