plugins {
    id("com.android.library")
//    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.hyperring.ringofrings.core"
    compileSdk = 34

    defaultConfig {
//        applicationId = "com.hyperring.ringofrings.core"
        minSdk = 25
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hyperring.core)
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha03")
    implementation("org.bitcoinj:bitcoinj-core:0.15.10")
    implementation("org.web3j:core:4.8.9-android")
    implementation("com.google.crypto.tink:tink-android:1.6.1")
//    implementation("com.google.crypto.tink:tink:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}