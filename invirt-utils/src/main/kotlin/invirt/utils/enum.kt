package invirt.utils

inline fun <reified E : Enum<E>> valueOfOrNull(name: String): E? = enumValues<E>().firstOrNull { it.name == name }

/**
 * Parses a comma separated string of enum names into a list of enum values
 */
inline fun <reified E : Enum<E>> String?.toEnumValues(): List<E> {
    if (this.isNullOrBlank()) {
        return emptyList()
    }
    return this.split(",")
        .map { it.trim() }
        .map { enumValueOf<E>(it) }
}
