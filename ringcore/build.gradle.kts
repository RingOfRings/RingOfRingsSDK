plugins {
//    id("com.android.library")
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
}

version = "1.0.7"
group = "com.github.RingOfRings"

android {
    namespace = "com.ringofrings.ringofrings.core"
    compileSdk = 35
    lint {
        abortOnError = false
        checkReleaseBuilds = false

    }

    defaultConfig {
        minSdk = 28
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }
    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources.excludes.apply {
            resources.excludes.add("META-INF/versions/**")
            resources.excludes.add("META-INF/**")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
//    implementation(libs.web3jcore)
//    implementation(libs.bitcoinjCore)
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha03")
    implementation("org.bitcoinj:bitcoinj-core:0.15.10")
    implementation("org.web3j:core:4.8.7")
//    implementation("com.google.crypto.tink:tink-android:1.6.1")
//    implementation("com.google.crypto.tink:tink:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}

configurations.all {
    resolutionStrategy {
        // 특정 모듈의 버전을 강제로 지정
        force("org.bouncycastle:bcprov-jdk15on:1.68")
    }
    // 필요한 경우 중복 모듈을 제외
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filesMatching("lint-resources.xml") {
        exclude()
    }
}