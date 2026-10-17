import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing.
//
// Reads keystore.properties from the repo root when present, which is how a
// developer machine signs. Both that file and the keystore are gitignored, so
// the key never enters version control. CI has no such file and instead gets
// the same four values from environment variables, which the workflow
// populates from repository secrets.
//
// If neither source is present the release variant is simply left unsigned
// rather than failing the build, so a fresh clone can still compile. That
// means `assembleRelease` produces an un-installable APK, which is exactly what
// it did before this existed -- a silent failure is worse than a loud one, so
// the build logs a warning.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingValue(localKey: String, envKey: String): String? =
    (keystoreProperties.getProperty(localKey) ?: System.getenv(envKey))?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("storeFile", "ANDROID_KEYSTORE_PATH")
val releaseStorePassword = signingValue("storePassword", "ANDROID_STORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "ANDROID_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "ANDROID_KEY_PASSWORD")

val hasReleaseSigning =
    releaseStoreFile != null && releaseStorePassword != null &&
        releaseKeyAlias != null && releaseKeyPassword != null

if (!hasReleaseSigning) {
    logger.lifecycle(
        "WARNING: no release signing configured (looked for keystore.properties " +
            "and the ANDROID_* environment variables). assembleRelease will " +
            "produce an unsigned APK that cannot be installed."
    )
}

android {
    namespace = "com.parallax.parallaxlauncher"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.parallax.parallaxlauncher"
        minSdk = 27
        targetSdk = 37
        // versionCode must strictly increase for Android to accept a build as
        // an update over an installed one. v1.0.0 shipped as 1 and has already
        // been downloaded, so v1.0.1 has to be 2.
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.rules)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}