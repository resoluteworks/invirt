package invirt.http4k

import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.http4k.lens.BiDiBodyLens

inline fun <reified T : Any> jsonLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()
