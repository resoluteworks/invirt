package invirt.utils.threads

/**
 * Runs the specified [block] by first setting the specified [value] on this [ThreadLocal] and
 * then clearing it after the block has finished.
 */
fun <T, R> ThreadLocal<T>.withValue(value: T, block: () -> R): R {
    this.set(value)
    val result = try {
        block()
    } finally {
        this.remove()
    }
    return result
}
