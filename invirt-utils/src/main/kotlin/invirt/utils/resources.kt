package invirt.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

fun resourceAsInput(classpathLocation: String): InputStream {
    return Thread.currentThread().contextClassLoader.getResourceAsStream(classpathLocation)!!
}

fun resourceAsString(classpathLocation: String, charset: Charset = Charsets.UTF_8): String {
    return resourceAsInput(classpathLocation).use { BufferedReader(InputStreamReader(it, charset)).readText() }
}

fun resourceAsProps(classpathLocation: String, charset: Charset = Charsets.UTF_8): Properties {
    val props = Properties()
    props.load(InputStreamReader(resourceAsInput(classpathLocation), charset))
    return props
}

fun resourceAsStrings(classpathLocation: String, charset: Charset = Charsets.UTF_8): List<String> {
    return resourceAsString(classpathLocation, charset).split("\n").map { it.cleanWhitespace() }
        .filter { it.isNotEmpty() }
}
