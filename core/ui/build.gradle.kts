plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.compose)
    id("urent.quality")
    id("urent.testing")
}

android {
    namespace = "info.jukov.urenttestassessment.core.ui"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions { unitTests.isIncludeAndroidResources = true }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
