import com.android.build.api.dsl.Packaging

plugins {
    id("org.jetbrains.kotlin.android") version "2.0.21" // or matching Kotlin 2.x version
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.android.application")
}

android {
    namespace = "com.example.insightsapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.insightsapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        compose = true                      // Enable Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.21"  // Use stable Compose compiler version
    }

    // To avoid packaging conflicts
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("androidx.compose.ui:ui:1.9.0")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text)

    debugImplementation("androidx.compose.ui:ui-tooling:1.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}