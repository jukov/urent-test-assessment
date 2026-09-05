plugins {
    id("urent.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "info.jukov.urenttestassestment.core.network"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    testImplementation(libs.okhttp.mockwebserver)
    api(libs.ktor.client.core)
    implementation(project(":core:logging"))
    implementation(libs.koin.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
}
