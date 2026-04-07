import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp) // KAPT 대신 KSP 사용
}

android {
    namespace = "com.roro.recorder"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Timber
    implementation(libs.timber)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Hilt + KSP
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 더 넓은 기기 지원 -> 동시 불가
    //implementation("com.google.android.gms:play-services-mlkit-speech-recognition:17.0.0")
    // 또는 GenAI 버전 -> AudioRecord 하나만 마이크를 잡고, PCM 버퍼를 직접 ML Kit에 넘기는 방식
    //implementation("com.google.mlkit:genai-speech-recognition:1.0.0-alpha1")
    implementation("com.google.mlkit:genai-summarization:1.0.0-beta1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0")

    //  Prompt API가 S25에서 아직 미지원 -> 혹시 모르니까...
//    implementation("com.google.mlkit:genai-prompt:1.0.0-beta1")

    // stt 관련
    implementation("com.google.mlkit:genai-speech-recognition:1.0.0-alpha1")

    // 번역 관련
    implementation("com.google.mlkit:translate:17.0.3")


}