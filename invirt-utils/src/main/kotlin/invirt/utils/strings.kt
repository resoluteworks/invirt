package invirt.utils

import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil

val REGEX_NON_ALPHA = "[^a-zA-Z\\d]+".toRegex()
val REGEX_WHITESPACE = "\\s+".toRegex()
val REGEX_ALPHA_AND_DASH_STARTS_WITH_LETTER = "[a-z][a-z\\d-]+".toRegex()
private val REGEX_WORD = "\\W+".toRegex()

fun String.wordCount(): Int = this.split(REGEX_WORD)
    .filter { it.isNotBlank() }
    .size

private val randomStringChars = ('a'..'z') + ('0'..'9')

/**
 * The reading time in minutes for a word count
 */
fun readingTimeMinutes(wordCount: Int): Int = ceil(wordCount / 265.0)
    .toInt()
    .coerceAtLeast(1)

fun String.urlEncode(charset: Charset = StandardCharsets.UTF_8): String = URLEncoder.encode(this, charset.name())

fun String.cleanWhitespace(): String = replace(REGEX_WHITESPACE, " ").trim()

fun String.titleCaseFirstChar(locale: Locale = Locale.getDefault()): String = replaceFirstChar { it.titlecase(locale) }

fun String.cleanEmail(): String = cleanWhitespace().lowercase()

fun String.ellipsis(maxLength: Int): String = if (this.length > maxLength) {
    this.take(maxLength - 3) + "..."
} else {
    this
}

fun String?.nullIfBlank(): String? = if (this != null && this.isBlank()) {
    null
} else {
    this
}

fun String.anonymizeEmail(visibleEndChars: Int = 0): String {
    val elements = this.split("@")
    return elements[0].anonymize(visibleEndChars) + "@" + elements[1].anonymize(visibleEndChars)
}

fun String.anonymize(visibleEndChars: Int = 0): String {
    val visibleStartIndex = (this.length - visibleEndChars).coerceAtLeast(0)
    return String(
        this
            .toCharArray()
            .mapIndexed { index, char ->
                if (index < visibleStartIndex) '*' else char
            }
            .toCharArray()
    )
}

fun <E : Enum<E>> Enum<E>.enumLabel(): String = this.name.enumLabel()

fun String.enumLabel(): String = this.replace("_", " ").lowercase().titleCaseFirstChar()

private val PATTERN_KEBAB = Pattern.compile("-([a-z])")
private val REGEX_CAMEL = "([a-z0-9])([A-Z])".toRegex()

fun String.camelToKebabCase(): String = this.replace(REGEX_CAMEL, "$1-$2").lowercase()

fun String.kebabToCamelCase(): String = PATTERN_KEBAB
    .matcher(this)
    .replaceAll { mr -> mr.group(1).uppercase() }

private val REGEX_DOMAIN = "(/.*)|(:.*)|(.*://)".toRegex()
fun String.domain(): String = this.lowercase().replace(REGEX_DOMAIN, "")
