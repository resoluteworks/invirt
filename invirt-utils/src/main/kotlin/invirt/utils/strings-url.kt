package invirt.utils

// Case-insensitive (RegexOption.IGNORE_CASE): scheme, `www.`, host and TLD all match regardless of
// case. Matches an optional `http(s)://` and/or `www.` prefix, a `host.tld`, and an optional
// path/query tail.
private val REGEX_URL = """(https?://)?(www\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\.[a-z]{2,32}\b([-a-zA-Z0-9@:%_+.~#?&/=]*)"""
    .toRegex(RegexOption.IGNORE_CASE)

/** True when the entire string is a URL. Use [containsUrl] to detect a URL embedded in free text. */
fun String.isUrl(): Boolean = matches(REGEX_URL)

/** True when the string contains a URL anywhere, e.g. a link pasted into a sentence. */
fun String.containsUrl(): Boolean = REGEX_URL.containsMatchIn(this)

fun String.httpUrl(https: Boolean = true): String = if (this.startsWith("http://") || this.startsWith("https://")) {
    this
} else {
    if (https) "https://$this" else "http://$this"
}
