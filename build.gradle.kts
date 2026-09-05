plugins {
    id("urent.quality")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("checkBuildLogic") {
    group = "verification"
    description = "Runs tests, plugin validation and static checks for convention plugins."
    dependsOn(gradle.includedBuild("build-logic").task(":check"))
}
