import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        // Works with Android's built-in Kotlin and plain JVM modules.
        configurations.matching { it.name == "testImplementation" }.all {
            libs.findBundle("jvm-testing").get().get().forEach { dependencies.add(it) }
        }
        tasks.withType<Test>().configureEach {
            useJUnit()
            testLogging {
                events("failed", "skipped")
                exceptionFormat = TestExceptionFormat.FULL
            }
            reports {
                junitXml.required.set(true)
                html.required.set(true)
            }
        }
    }
}
