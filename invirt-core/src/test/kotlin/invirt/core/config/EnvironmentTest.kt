package invirt.core.config

import invirt.utils.uuid7
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvEntry
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.http4k.config.Environment
import java.io.File
import java.io.FileNotFoundException

class EnvironmentTest : StringSpec({

    "Environment.withDotEnv(Dotenv)" {
        val dotenv = mockk<Dotenv>()
        every { dotenv.entries() } returns setOf(DotenvEntry("key", "from-dot-env"))
        Environment.from("key" to "from-env").withDotEnv(dotenv)["key"] shouldBe "from-env"
        Environment.from("other" to "value").withDotEnv(dotenv)["key"] shouldBe "from-dot-env"
    }

    "Environment.withDotEnv(dotEnvFile) standard .env filename" {
        val dotEnvFile = File(tempdir(), ".env")
        val varValue = uuid7()
        dotEnvFile.writeText("AN_ENVIRONMENT_VARIABLE=$varValue\n")
        Environment.ENV.withDotEnv(dotEnvFile.absolutePath)["AN_ENVIRONMENT_VARIABLE"] shouldBe varValue
    }

    "Environment.withDotEnv(dotEnvFile) custom filename" {
        listOf(".env.local", ".env.test", "app.env").forEach { filename ->
            val dotEnvFile = File(tempdir(), filename)
            val varValue = uuid7()
            dotEnvFile.writeText("AN_ENVIRONMENT_VARIABLE=$varValue\n")
            Environment.ENV.withDotEnv(dotEnvFile.absolutePath)["AN_ENVIRONMENT_VARIABLE"] shouldBe varValue
        }
    }

    "Environment.withDotEnv(dotEnvFile) nested directory path" {
        val dotEnvFile = File(tempdir(), "config/local/.env")
        dotEnvFile.parentFile.mkdirs()
        val varValue = uuid7()
        dotEnvFile.writeText("AN_ENVIRONMENT_VARIABLE=$varValue\n")
        Environment.ENV.withDotEnv(dotEnvFile.absolutePath)["AN_ENVIRONMENT_VARIABLE"] shouldBe varValue
    }

    "Environment.withDotEnv(dotEnvFile) missing file leaves environment unchanged" {
        val dotEnvFile = File(tempdir(), ".env.missing")
        Environment.ENV.withDotEnv(dotEnvFile.absolutePath)["AN_ENVIRONMENT_VARIABLE"] shouldBe null
    }

    "developmentMode" {
        Environment.EMPTY.developmentMode shouldBe false
        Environment.from("DEVELOPMENT_MODE" to "false").developmentMode shouldBe false
        Environment.from("DEVELOPMENT_MODE" to "true").developmentMode shouldBe true
    }

    "gitCommitId" {
        mockkObject(Environment) {
            val commitId = uuid7()
            every { Environment.fromResource("git.properties") } returns Environment.from("git.commit.id" to commitId)
            gitCommitId() shouldBe commitId
        }
    }

    "gitCommitId null when file is empty" {
        mockkObject(Environment) {
            every { Environment.fromResource("git.properties") } returns Environment.EMPTY
            gitCommitId() shouldBe null
        }
    }

    "gitCommitId throws exception when file doesn't exist" {
        mockkObject(Environment) {
            every { Environment.fromResource("git.properties") } answers {
                throw FileNotFoundException("git.properties")
            }
            shouldThrowWithMessage<FileNotFoundException>("git.properties") {
                gitCommitId()
            }
        }
    }
})
