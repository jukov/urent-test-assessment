import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class QualityConventionPluginTest {
    @get:Rule
    val projectDirectory = TemporaryFolder()

    @Before
    fun createProject() {
        val repository = File(System.getProperty("repositoryRoot"))
        write("gradle/libs.versions.toml", repository.resolve("gradle/libs.versions.toml").readText())
        write("config/detekt/detekt.yml", repository.resolve("config/detekt/detekt.yml").readText())
        write(".editorconfig", repository.resolve(".editorconfig").readText())
        write("settings.gradle", "rootProject.name = 'quality-fixture'\n")
        write(
            "build.gradle",
            """
            plugins {
                id 'urent.quality'
            }
            repositories { mavenCentral() }
            """.trimIndent()
        )
    }

    @Test
    fun `detekt rejects violations in androidTest sources`() {
        write("src/androidTest/kotlin/BadName.kt", "class incorrectly_named\n")

        val result = runner("detekt").buildAndFail()

        assertEquals(TaskOutcome.FAILED, result.task(":detekt")?.outcome)
        assertTrue(result.output.contains("ClassNaming"))
    }

    @Test
    fun `ktlint rejects incorrectly formatted build scripts`() {
        write("bad.gradle.kts", "val bad=1\n")

        val result = runner("ktlintCheck").buildAndFail()

        assertTrue(result.output.contains("ktlintKotlinScriptCheck FAILED"))
    }

    @Test
    fun `generated build output is excluded from checks`() {
        write("build/generated/bad.gradle.kts", "val bad=1\n")
        write("build/generated/BadName.kt", "class incorrectly_named\n")

        runner("ktlintCheck", "detekt").build()
    }

    private fun runner(vararg tasks: String): GradleRunner = GradleRunner.create()
        .withProjectDir(projectDirectory.root)
        .withPluginClasspath()
        .withArguments(*tasks, "--stacktrace", "--console=plain", "--max-workers=2")

    private fun write(path: String, content: String) {
        projectDirectory.root.resolve(path).apply {
            parentFile.mkdirs()
            writeText(content)
        }
    }
}
