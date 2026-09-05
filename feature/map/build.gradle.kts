import org.gradle.api.tasks.testing.Test

plugins {
    id("urent.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "info.jukov.urenttestassestment.feature.map"
    buildFeatures { compose = true }
    defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.maplibre)
    implementation(libs.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}

// Exercise the exact archive shipped in the APK; track it as a test input for Gradle caching.
val markerFontArchive = layout.projectDirectory.file("src/main/assets/fonts/roboto-bold-glyphs.zip")
tasks.withType<Test>().configureEach {
    inputs.file(markerFontArchive)
    systemProperty("markerFontArchive", markerFontArchive.asFile.absolutePath)
}
