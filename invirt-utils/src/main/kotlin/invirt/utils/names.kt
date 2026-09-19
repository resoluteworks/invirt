package invirt.utils

/**
 * The first whitespace-separated token of this name, or [fallback] when the name is blank.
 *
 * Only the first token is read, and the tail is never inspected: reading past it mangles whole
 * classes of real input, since a compound surname ("Jan van der Berg"), a suffix ("Rachel Kim,
 * PhD"), or a parenthetical ("Rachel (member 1)") would all leave a fragment behind if split
 * further. Treating the first token as the given name holds for a name written given-name-first,
 * including a compound given name such as "Mary Jane Watson" (the first token "Mary" is still a
 * whole word), but this is a culture-bound assumption: a name with no internal whitespace, such
 * as "陳大文", is returned whole rather than split at all.
 */
fun String.greetingName(fallback: String): String =
    trim().split(REGEX_WHITESPACE).firstOrNull { it.isNotEmpty() } ?: fallback

/**
 * Splits this name into a (firstName, lastName) pair by cutting after the first
 * whitespace-separated token: the first token, and every token after it re-joined with single
 * spaces. Both parts are null when the name is blank, and the last name is null for a
 * single-token name ("Cher" gives "Cher" to null).
 *
 * The cut is after the first token rather than before the last one, so neither part is ever a
 * fragment of a word group: "Rachel (member 1)" gives "Rachel" to "(member 1)", and "Jan van der
 * Berg" gives "Jan" to "van der Berg". The trade-off is that a compound given name lands on the
 * last-name side ("Mary Jane Watson" gives "Mary" to "Jane Watson").
 */
fun String.splitName(): Pair<String?, String?> {
    val tokens = trim().split(REGEX_WHITESPACE).filter { it.isNotEmpty() }
    return tokens.firstOrNull() to tokens.drop(1).joinToString(" ").ifBlank { null }
}
