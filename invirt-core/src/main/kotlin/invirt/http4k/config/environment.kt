package invirt.http4k.config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int

fun Environment.withDotEnv(dotEnvDirectory: String = "./"): Environment {
    return withDotEnv(
        dotenv {
            directory = dotEnvDirectory
            ignoreIfMissing = true
            systemProperties = false
        }
    )
}

fun Environment.withDotEnv(dotEnv: Dotenv): Environment {
    return this overrides Environment.from(dotEnv.entries().associate { it.key to it.value })
}

/**
 * Boolean set with DEVELOPMENT_MODE
 */
val Environment.developmentMode: Boolean get() = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(this)

/**
 * Reads "git.commit.id" from git.properties in the classpath. Requires gradle plugin id("com.gorylenko.gradle-git-properties")
 */
val gitCommitHash: String get() = Environment.fromResource("git.properties")["git.commit.id"]!!

/**
 * Application port set via APPLICATION_PORT
 */
fun Environment.applicationPort(default: Int = 8080): Int = EnvironmentKey.int().defaulted("APPLICATION_PORT", default)(this)
