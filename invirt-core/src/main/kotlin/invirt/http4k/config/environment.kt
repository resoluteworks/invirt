package invirt.http4k.config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int

fun Environment.withDotEnv(dotEnvDirectory: String = "./"): Environment = withDotEnv(
    dotenv {
        directory = dotEnvDirectory
        ignoreIfMissing = true
        systemProperties = false
    }
)

fun Environment.withDotEnv(dotEnv: Dotenv): Environment = this overrides Environment.from(dotEnv.entries().associate { it.key to it.value })

/**
 * Boolean set with DEVELOPMENT_MODE
 */
val Environment.developmentMode: Boolean get() = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(this)

/**
 * Reads "git.commit.id" from git.properties in the classpath.
 *
 * Requires Gradle plugin id("com.gorylenko.gradle-git-properties") or some other mechanism to
 * generate a classpath:git.properties with a git.commit.id property
 */
fun gitCommitId(): String? = EnvironmentKey.optional("git.commit.id")(Environment.fromResource("git.properties"))

/**
 * Application port set via APPLICATION_PORT
 */
fun Environment.applicationPort(default: Int = 8080): Int = EnvironmentKey.int().defaulted("APPLICATION_PORT", default)(this)
