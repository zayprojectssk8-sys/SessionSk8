plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sk8.appwatch"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.zayprojetcs.weeksk8"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.compose.ui.tooling)
    implementation(libs.play.services.wearable)

    implementation(libs.kotlinx.coroutines.play.services)

    // Si estás creando UI nativa para Wear OS en Compose:
    implementation("androidx.wear.compose:compose-material:1.6.2")
    implementation("androidx.wear.compose:compose-foundation:1.6.2")
    implementation("androidx.wear.compose:compose-navigation:1.6.2")

    // Health Services API para Wear OS (lectura de sensores y ejercicio)
    implementation("androidx.health.connect:connect-client:1.1.0")
    implementation("androidx.health:health-services-client:1.0.0")

    // Soporte de Coroutines para Guava (.await() en ListenableFuture)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.11.0")

    // (Opcional recomendado) Extensiones de Kotlin para Futures
    implementation("androidx.concurrent:concurrent-futures-ktx:1.3.0")

    implementation("androidx.lifecycle:lifecycle-service:2.8.7")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}