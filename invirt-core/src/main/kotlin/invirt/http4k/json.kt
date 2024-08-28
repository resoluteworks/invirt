package invirt.http4k

import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.http4k.lens.BiDiBodyLens

/**
 * Convenience function to create a JSON body lens for a type.
 */
inline fun <reified T : Any> jsonLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()
