import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        pluginManager.apply("dev.detekt")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        extensions.configure<KtlintExtension> {
            version.set(libs.findVersion("ktlint").get().requiredVersion)
            android.set(true)
            outputToConsole.set(true)
            ignoreFailures.set(false)
            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.CHECKSTYLE)
            }
            filter { exclude("**/build/**") }
        }
        extensions.configure<DetektExtension> {
            toolVersion.set(libs.findVersion("detekt").get().requiredVersion)
            buildUponDefaultConfig.set(true)
            ignoreFailures.set(false)
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            // Include every source set, including androidTest and future flavors.
            source.setFrom(fileTree("src") { include("**/*.kt", "**/*.kts") })
        }
        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> { lint { configureLint(this, target) } }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> { lint { configureLint(this, target) } }
        }
    }

    private fun configureLint(lint: Lint, project: Project) = with(lint) {
        lintConfig = project.rootProject.file("config/lint.xml")
        abortOnError = true
        warningsAsErrors = true
        checkTestSources = true
    }
}
