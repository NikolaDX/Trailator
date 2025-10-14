import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")
}

buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

android {
    namespace = "com.nikoladx.trailator"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nikoladx.trailator"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        val mapsApiKey = properties.getProperty("MAPS_API_KEY") ?: ""
        val cloudinaryName = properties.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""
        val cloudinaryApiKey = properties.getProperty("CLOUDINARY_API_KEY") ?: ""
        val cloudinaryApiSecret = properties.getProperty("CLOUDINARY_API_SECRET") ?: ""
        val cloudinaryUploadPreset = properties.getProperty("CLOUDINARY_UPLOAD_PRESET") ?: ""

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")

    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Maps
    implementation("com.google.maps.android:maps-compose:6.12.1")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.1.2")

    // Material
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Jetpack compose
    implementation("io.coil-kt:coil-compose:2.7.0")
}