plugins {
    id("urent.android.library")
}

android {
    namespace = "info.jukov.urenttestassestment.core.logging"
}

dependencies {
    implementation(libs.timber)
}
