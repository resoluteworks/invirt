package invirt.utils

private val REGEX_URL = """(http(s)?://.)?(www\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\.[a-z]{2,32}\b([-a-zA-Z0-9@:%_+.~#?&/=]*)""".toRegex()

fun String.isUrl(): Boolean = matches(REGEX_URL)

fun String.httpUrl(https: Boolean = true): String = if (this.startsWith("http://") || this.startsWith("https://")) {
    this
} else {
    if (https) "https://$this" else "http://$this"
}
