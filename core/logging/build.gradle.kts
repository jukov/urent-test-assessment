plugins {
    id("urent.android.library")
}

android {
    namespace = "info.jukov.urenttestassessment.core.logging"
}

dependencies {
    implementation(libs.timber)
}
