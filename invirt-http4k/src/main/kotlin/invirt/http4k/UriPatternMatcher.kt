package invirt.http4k

import org.http4k.core.Request

/**
 * A crude implementation for a URI path pattern matcher. The patterns are typically used to indicate
 * that a URI starts with the pattern.
 * For example "/login" will match "/login", "/login?a=b", "/login/me", "/login/me?param=true"
 *
 * Regular expressions can be used as well:
 * Example: "/auth.*" will match "/authenticate", "/authorise", "/auth/1", "/authorise/1"
 *
 * This isn't usable for complex logic like matching for "/login" but not for "/login/dont-match-this"
 */
data class UriPatternMatcher(val patterns: Set<String>) {

    constructor(vararg patterns: String) : this(patterns.toSet())

    private val lowercasePatterns: Set<String> = patterns.map { it.lowercase() }.toSet()
    private val regexs: List<Regex> = lowercasePatterns.map { it.toUriPathRegex() }

    init {
        lowercasePatterns.forEach { pattern ->
            val conflict =
                lowercasePatterns.find { it != pattern && (it.startsWith(pattern) || pattern.startsWith(it)) }
            if (conflict != null) {
                throw IllegalArgumentException("Conflicting patterns: '${pattern}' vs '${conflict}'")
            }
        }
    }

    fun matches(uri: String): Boolean {
        return regexs.isNotEmpty() && regexs.any { it.matches(uri.lowercase()) }
    }

    fun matches(request: Request): Boolean {
        return matches(request.uri.path)
    }
}

fun Set<String>.toUriPatternMatcher(): UriPatternMatcher {
    return UriPatternMatcher(this)
}

private fun String.toUriPathRegex(): Regex {
    val lowercase = this.lowercase()
    return "$lowercase((/.*)|(\\?.*)|$)".toRegex()
}
