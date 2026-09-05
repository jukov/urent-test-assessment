import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("urent.quality")
        pluginManager.apply("urent.testing")

        extensions.configure<LibraryExtension> {
            compileSdk {
                version = release(COMPILE_SDK)
            }
            defaultConfig {
                minSdk = MIN_SDK
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }

    private companion object {
        const val COMPILE_SDK = 37
        const val MIN_SDK = 28
    }
}
