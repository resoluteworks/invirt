package invirt.http4k.config

import invirt.utils.uuid7
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvEntry
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.http4k.cloudnative.env.Environment
import java.io.File

class EnvironmentTest : StringSpec({

    "Environment.withDotEnv(Dotenv)" {
        val dotenv = mockk<Dotenv>()
        every { dotenv.entries() } returns setOf(DotenvEntry("key", "from-dot-env"))
        Environment.from("key" to "from-env").withDotEnv(dotenv)["key"] shouldBe "from-env"
        Environment.from("other" to "value").withDotEnv(dotenv)["key"] shouldBe "from-dot-env"
    }

    "Environment.withDotEnv(dotEnvDirectory)" {
        val dir = tempdir()
        val dotEnvFile = File(dir, ".env")
        val varValue = uuid7()
        dotEnvFile.writeText("AN_ENVIRONMENT_VARIABLE=$varValue\n")
        Environment.ENV.withDotEnv(dir.absolutePath)["AN_ENVIRONMENT_VARIABLE"] shouldBe varValue
    }

    "developmentMode" {
        Environment.EMPTY.developmentMode shouldBe false
        Environment.from("DEVELOPMENT_MODE" to "false").developmentMode shouldBe false
        Environment.from("DEVELOPMENT_MODE" to "true").developmentMode shouldBe true
    }

    "gitCommitHash" {
        mockkObject(Environment) {
            val commitId = uuid7()
            every { Environment.fromResource("git.properties") } returns Environment.from("git.commit.id" to commitId)
            gitCommitHash shouldBe commitId
        }
    }

    "applicationPort" {
        Environment.EMPTY.applicationPort() shouldBe 8080
        Environment.EMPTY.applicationPort(2020) shouldBe 2020
        Environment.from("APPLICATION_PORT" to "9090").applicationPort() shouldBe 9090
        Environment.from("APPLICATION_PORT" to "9090").applicationPort(2020) shouldBe 9090
    }
})
