plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("urent.quality")
    id("urent.testing")
}

// Only tags on HEAD identify a release; an ancestor tag must not label newer code.
val headTags = providers.exec {
    workingDir(rootDir)
    commandLine("git", "tag", "--points-at", "HEAD", "--list", "v*")
}.standardOutput.asText.get().lineSequence().filter { it.isNotBlank() }.toList()
require(headTags.size <= 1) { "Multiple release tags on HEAD: $headTags" }
val candidateTag = providers.gradleProperty("candidateReleaseTag").orNull
val headTag = headTags.singleOrNull()
require(candidateTag == null || headTag == null || candidateTag == headTag) {
    "candidateReleaseTag must match the release tag on HEAD"
}
val releaseTag = headTag ?: candidateTag
val releaseVersion = ReleaseVersion.fromTag(releaseTag)
val signingPropertiesFile = rootProject.file(
    providers.gradleProperty("signingProperties").getOrElse("keys/release.properties")
)
val releaseSigning = ReleaseSigning.load(signingPropertiesFile)

android {
    namespace = "info.jukov.urenttestassestment"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "info.jukov.urenttestassestment"
        minSdk = 28
        targetSdk = 37
        versionCode = releaseVersion.code
        versionName = releaseVersion.name

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigning != null) {
            create("release") {
                storeFile = releaseSigning.storeFile
                storePassword = releaseSigning.storePassword
                keyAlias = releaseSigning.keyAlias
                keyPassword = releaseSigning.keyPassword
            }
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            optimization {
                enable = true
                keepRules { includeDefault = true }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set(output.versionName.map { "urent_jukov_v$it.apk" })
        }
    }
}

dependencies {
    implementation(project(":core:logging"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":feature:cities"))
    implementation(project(":feature:map"))
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.lifecycle.viewmodel.compose)
    androidTestImplementation(project(":core:model"))
    androidTestImplementation(project(":core:pagination"))
    androidTestImplementation(libs.ktor.client.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
