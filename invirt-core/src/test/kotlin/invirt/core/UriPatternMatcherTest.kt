package invirt.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request

class UriPatternMatcherTest : StringSpec({

    "basic matching" {
        val matcher = UriPatternMatcher("/authenticatE")
        matcher.matches("/authenticate") shouldBe true
        matcher.matches("/authenticate/") shouldBe true
        matcher.matches("/autheNticAte/") shouldBe true
        matcher.matches("/AUTHENTICATE/") shouldBe true
        matcher.matches("/authenticate/something") shouldBe true
        matcher.matches("/authenticate/SOMETHING") shouldBe true
        matcher.matches("/AUTHENTICATE/someTHING") shouldBe true
        matcher.matches("/authenticate/123") shouldBe true
        matcher.matches("/authenticate?") shouldBe true
        matcher.matches("/authenticate?something") shouldBe true
        matcher.matches("/authenticate?something=2") shouldBe true
        matcher.matches("/authenticate?token_type=magic_links&token=lk234ljk234k2n4k23n2l3k4n23lk4") shouldBe true

        matcher.matches("/authenticate+") shouldBe false
        matcher.matches("/authenticateme") shouldBe false
        matcher.matches("/authenticate-again") shouldBe false
        matcher.matches("/authenticate.page") shouldBe false
        matcher.matches("/authenticate123") shouldBe false
        matcher.matches("/authenticate(3)") shouldBe false
    }

    "pattern matching with *" {
        val matcher = UriPatternMatcher("/auth*")
        matcher.matches("/auth") shouldBe true
        matcher.matches("/authenticate") shouldBe true
        matcher.matches("/authorise") shouldBe true
        matcher.matches("/auth/1") shouldBe true
        matcher.matches("/authorise/1") shouldBe true
        matcher.matches("/nonauth") shouldBe false
    }

    "pattern matching with /*" {
        val matcher = UriPatternMatcher("/auth/*")
        matcher.matches("/auth") shouldBe false
        matcher.matches("/authenticate") shouldBe false
        matcher.matches("/authorise") shouldBe false
        matcher.matches("/auth/1") shouldBe true
        matcher.matches("/authorise/1") shouldBe false
    }

    "@" {
        val matcher = UriPatternMatcher("/@*")
        matcher.matches("/@john-smith") shouldBe true
        matcher.matches("/@john-smith/abcd") shouldBe true
        matcher.matches("/@mack") shouldBe true
        matcher.matches("/@mack/123bb?a=b") shouldBe true
    }

    "multiple patterns" {
        val matcher = UriPatternMatcher("/a", "/b/c")
        matcher.matches("/a") shouldBe true
        matcher.matches("/a?param=true") shouldBe true
        matcher.matches("/b/c") shouldBe true
        matcher.matches("/b/c?param=true") shouldBe true

        matcher.matches("/b") shouldBe false
        matcher.matches("/c") shouldBe false
        matcher.matches("/something-else") shouldBe false
    }

    "deep patterns" {
        val matcher = UriPatternMatcher("/auth/login/user")
        matcher.matches("/auth/login/user") shouldBe true
        matcher.matches("/auth/login/user/myuser") shouldBe true
        matcher.matches("/auth/login/user?a=b&c=d") shouldBe true
        matcher.matches("/auth/login/user/myuser?a=b&c=d") shouldBe true

        matcher.matches("/auth") shouldBe false
        matcher.matches("/auth?a=b") shouldBe false
        matcher.matches("/auth/login") shouldBe false
        matcher.matches("/auth/login?a=b") shouldBe false
    }

    "empty matcher" {
        UriPatternMatcher().matches("/") shouldBe false
        UriPatternMatcher().matches("/a") shouldBe false
        UriPatternMatcher().matches("/a/b") shouldBe false
    }

    "conflicts" {
        shouldThrow<IllegalArgumentException> {
            UriPatternMatcher("/a", "/a/b")
        }
        shouldThrow<IllegalArgumentException> {
            UriPatternMatcher("/a/b", "/a")
        }
    }

    "match request" {
        val matcher = UriPatternMatcher("/auth/login/user")
        matcher.matches(Request(Method.GET, "/auth/login/user")) shouldBe true
        matcher.matches(Request(Method.GET, "/auth/login/user/myuser")) shouldBe true
        matcher.matches(Request(Method.GET, "/auth/login/user?a=b&c=d")) shouldBe true
        matcher.matches(Request(Method.GET, "/auth/login/user/myuser?a=b&c=d")) shouldBe true

        matcher.matches(Request(Method.GET, "/auth")) shouldBe false
        matcher.matches(Request(Method.GET, "/auth?a=b")) shouldBe false
        matcher.matches(Request(Method.GET, "/auth/login")) shouldBe false
        matcher.matches(Request(Method.GET, "/auth/login?a=b")) shouldBe false
    }

    "/auth/* should match /auth/ /auth/1 /auth/something/else but not /authenticate" {
        val matcher = UriPatternMatcher("/auth/*")
        matcher.matches("/auth/") shouldBe true
        matcher.matches("/auth/1") shouldBe true
        matcher.matches("/auth/something/else") shouldBe true
        matcher.matches("/authenticate") shouldBe false
    }
})
