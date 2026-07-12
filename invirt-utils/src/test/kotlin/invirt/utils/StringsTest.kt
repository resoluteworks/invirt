package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class StringsTest : StringSpec({

    "wordCount" {
        "Sitting mistake towards his, few country ask".wordCount() shouldBe 7
        "Nay, impossible; dispatched! partiality - unaffected".wordCount() shouldBe 5
        "   \t \n Sitting mistake towards his, few country ask \n\n \t Nay, impossible; dispatched! partiality - unaffected \n."
            .wordCount() shouldBe 12
    }
    "regex tests" {
        "a^$.b=1".replace(REGEX_NON_ALPHA, "") shouldBe "ab1"
        "   a \t \n b \t1 ".replace(REGEX_WHITESPACE, "") shouldBe "ab1"
    }

    "domain" {
        "test.com".domain() shouldBe "test.com"
        "abc.test.com".domain() shouldBe "abc.test.com"
        "http://abc.test.com".domain() shouldBe "abc.test.com"
        "https://abc.test.com".domain() shouldBe "abc.test.com"
        "https://ABC.test.com/something".domain() shouldBe "abc.test.com"
        "https://abc.test.com:8080/something".domain() shouldBe "abc.test.com"
        "localHOST:8080".domain() shouldBe "localhost"
        "localhost:8080/something".domain() shouldBe "localhost"
    }

    "clean email" {
        "John.SMITH@GMail.com".cleanEmail() shouldBe "john.smith@gmail.com"
        " John.SMITH@GMail.com  \t".cleanEmail() shouldBe "john.smith@gmail.com"
    }

    "ellipsis" {
        "stop".ellipsis(5) shouldBe "stop"
        "stop".ellipsis(4) shouldBe "stop"
        "spice world".ellipsis(6) shouldBe "spi..."
    }

    "anonymize string" {
        "test".anonymize() shouldBe "****"
        "1234567893432345".anonymize() shouldBe "****************"
        "1234567893430146".anonymize(4) shouldBe "************0146"
        "test".anonymize(4) shouldBe "test"
        "test".anonymize(5) shouldBe "test"
        "test".anonymize(1) shouldBe "***t"
    }

    "anonymize email" {
        "john.smith@test.com".anonymizeEmail() shouldBe "**********@********"
        "john.smith@test.com".anonymizeEmail(4) shouldBe "******mith@****.com"
    }

    "urlEncode" {
        "computers & internet".urlEncode() shouldBe "computers+%26+internet"
    }

    "titleCaseFirstChar" {
        "something else".titleCaseFirstChar() shouldBe "Something else"
        "SOMETHING ELSE".titleCaseFirstChar() shouldBe "SOMETHING ELSE"
        "Ǆemal".titleCaseFirstChar(Locale("hr", "HR")) shouldBe "ǅemal"
        "ǆemal".titleCaseFirstChar() shouldBe "ǅemal"
        "".titleCaseFirstChar() shouldBe ""
    }

    "nullIfBlank" {
        "".nullIfBlank() shouldBe null
        "  ".nullIfBlank() shouldBe null
        "    \t \n".nullIfBlank() shouldBe null
        null.nullIfBlank() shouldBe null
        "aa".nullIfBlank() shouldBe "aa"
    }

    "kebab to camel case" {
        "company-information".kebabToCamelCase() shouldBe "companyInformation"
        "company".kebabToCamelCase() shouldBe "company"
    }

    "camel to kebab case" {
        "companyInformation".camelToKebabCase() shouldBe "company-information"
        "userIdentificationRecord".camelToKebabCase() shouldBe "user-identification-record"
        "company".camelToKebabCase() shouldBe "company"
        "company-information".camelToKebabCase() shouldBe "company-information"
    }

    "enum label" {
        StringsTestEnum.THIS.enumLabel() shouldBe "This"
        StringsTestEnum.SOMETHING_ELSE.enumLabel() shouldBe "Something else"
    }

    "enum label string" {
        "STARTED".enumLabel() shouldBe "Started"
        "NOT_STARTED".enumLabel() shouldBe "Not started"
    }

    "REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER" {
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("anchor-123") shouldBe true
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("theme123") shouldBe true
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("b-123-dash") shouldBe true
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("b-1356676") shouldBe true
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("x1356676") shouldBe true
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("abcd") shouldBe true

        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("anchor.123") shouldBe false
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("something.else") shouldBe false
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("something£else") shouldBe false
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("@somethingelse") shouldBe false
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("AbCd123") shouldBe false
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("12321321") shouldBe false
        REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER.matches("-abcde123") shouldBe false
    }

    "isUrl" {
        // Not URLs
        "".isUrl() shouldBe false
        "test".isUrl() shouldBe false
        "some words".isUrl() shouldBe false
        "test .com".isUrl() shouldBe false
        "http: // test.com".isUrl() shouldBe false
        "£25.00".isUrl() shouldBe false

        // URLs: with/without scheme, www, path and query
        "test.com".isUrl() shouldBe true
        "www.test.com".isUrl() shouldBe true
        "http://test.com".isUrl() shouldBe true
        "https://test.com".isUrl() shouldBe true
        "http://ab.com".isUrl() shouldBe true
        "sub.domain.test.com".isUrl() shouldBe true
        "https://test.artgallery".isUrl() shouldBe true
        "https://test.com/something/else.file".isUrl() shouldBe true
        "https://sommerhaus.gallery?param=123".isUrl() shouldBe true
        "https://sommerhaus.gallery/?a=test".isUrl() shouldBe true

        // Case-insensitive: scheme, www and TLD in any case
        "HTTP://TEST.COM".isUrl() shouldBe true
        "HTTPS://Test.Com".isUrl() shouldBe true
        "WWW.TEST.COM".isUrl() shouldBe true
        "Https://Test.Com/Path".isUrl() shouldBe true
        "TEST.COM".isUrl() shouldBe true
    }

    "containsUrl" {
        // Unlike isUrl (whole-string match), containsUrl finds a link anywhere in free text.
        "".containsUrl() shouldBe false
        "test".containsUrl() shouldBe false
        "some words".containsUrl() shouldBe false
        "no links here at all".containsUrl() shouldBe false
        // Plain prices stay clean: the fractional part is digits, not a letter TLD.
        "£25.00".containsUrl() shouldBe false
        "£25".containsUrl() shouldBe false
        "£0. Others pay £25".containsUrl() shouldBe false
        "e.g. £25".containsUrl() shouldBe false

        // A whole-string URL contains a URL
        "test.com".containsUrl() shouldBe true
        "www.test.com".containsUrl() shouldBe true
        "http://test.com".containsUrl() shouldBe true
        "https://test.com".containsUrl() shouldBe true
        // A URL embedded in surrounding text, in any case
        "pay at gallery.co.uk".containsUrl() shouldBe true
        "buy tickets at https://test.com/pay now".containsUrl() shouldBe true
        "Visit HTTPS://TEST.COM now".containsUrl() shouldBe true
        "email WWW.Test.Com please".containsUrl() shouldBe true
    }

    "httpUrl" {
        "test.com".httpUrl() shouldBe "https://test.com"
        "instagram.com".httpUrl() shouldBe "https://instagram.com"
        "instagram.com/myuser".httpUrl() shouldBe "https://instagram.com/myuser"
        "gallery.com/art/events.html".httpUrl() shouldBe "https://gallery.com/art/events.html"
        "gallery.com/art/events.html".httpUrl(false) shouldBe "http://gallery.com/art/events.html"
        "http://instagram.com".httpUrl() shouldBe "http://instagram.com"
        "https://instagram.com/myuser".httpUrl() shouldBe "https://instagram.com/myuser"
    }
})

private enum class StringsTestEnum {
    THIS,
    SOMETHING_ELSE
}
