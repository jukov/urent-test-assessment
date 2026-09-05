plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    testImplementation(libs.junit)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        register("quality") {
            id = "urent.quality"
            implementationClass = "QualityConventionPlugin"
        }
        register("testing") {
            id = "urent.testing"
            implementationClass = "TestingConventionPlugin"
        }
    }
}

ktlint {
    version.set(libs.versions.ktlint)
    android.set(true)
    filter { exclude("**/build/**") }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    config.setFrom(file("../config/detekt/detekt.yml"))
}

tasks.test {
    useJUnit()
    systemProperty("repositoryRoot", rootDir.parent)
}
