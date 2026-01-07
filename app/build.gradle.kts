plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.albarmajy.medscan"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.albarmajy.medscan"
        minSdk = 26
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
        compose = true
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
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.protolite.well.known.types)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    // 1. Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // 2. Room Database (التخزين المحلي)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion") // تأكد من إضافة plugin 'kotlin-kapt'

    // 3. Google ML Kit (الذكاء الاصطناعي للمسح الضوئي)
//    implementation("com.google.mlkit:text-recognition-arabic:16.0.0") // يدعم العربية والإنجليزي

    // 4. CameraX (لتشغيل الكاميرا داخل التطبيق)
    val cameraVersion = "1.3.0"
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // 5. Dependency Injection (Koin)
    implementation("io.insert-koin:koin-androidx-compose:3.5.0")

    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
}

