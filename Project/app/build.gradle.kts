// Plugin for gradle
plugins {
    alias(libs.plugins.android.application) // Plugin for android app
    alias(libs.plugins.kotlin.android) // Plugin for Kotlin android
    alias(libs.plugins.kotlin.compose) // Plugin for jetpack compose
    id("org.jetbrains.kotlin.kapt") // Annotation processing for glide
}

// Option for android app buile
android {
    namespace = "com.example.project" // Package name
    compileSdk = 35 // Android SDK version for compile

    defaultConfig {
        applicationId = "com.example.project" // App ID
        minSdk = 26 // minimum support android version
        targetSdk = 35 // Target android version
        versionCode = 1 // Inside version code
        versionName = "1.0" // Version name for user visible
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en") // Contain only english resource
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Release Code obfuscation & Disable optimization

            // Set proguard option file
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11" // Compile kotlin code by JVM 11 target
    }

    buildFeatures {
        compose = true // Set jetpack compose usage
    }
}

dependencies {
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Constraint layout
    implementation("androidx.appcompat:appcompat:1.6.1") // HTTP communication library
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // Support compatibility
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Print communication log
    implementation("com.github.bumptech.glide:glide:4.15.1") // Image loading
    kapt("com.github.bumptech.glide:compiler:4.15.1") // annotation processor for glide
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}