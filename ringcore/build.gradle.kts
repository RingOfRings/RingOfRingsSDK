plugins {
//    id("com.android.library")
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
}

version = "1.1.0"
group = "com.github.RingOfRings"


publishing {
    publications {
        create<MavenPublication>("library") {
//            from(components["java"])
            groupId = "com.github.RingOfRings"
            artifactId = "RingOfRingsSDK"
            version = "1.1.0"
            pom {
                name = "RingOfRings Core Library"
                description = "RingOfRings  core sdk library"
                url = "https://github.com/RingOfRings/RingOfRingsSDK"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("publishing-repository"))
        }
    }
}

android {
    namespace = "com.ringofrings.ringofrings.core"
    compileSdk = 35
    lint {
        abortOnError = false
        checkReleaseBuilds = false

    }

    defaultConfig {
        minSdk = 28

        consumerProguardFiles("consumer-rules.pro")
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
            resources.excludes.add("META-INF/LICENSE.md")
            resources.excludes.add("META-INF/LICENSE-notice.md")
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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filesMatching("lint-resources.xml") {
        exclude()
    }
}

configurations.all {
    resolutionStrategy {
        // 특정 모듈의 버전을 강제로 지정
        force("org.bouncycastle:bcprov-jdk15on:1.68")
    }
    // 필요한 경우 중복 모듈을 제외
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
}

afterEvaluate {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.bouncycastle") {
                useVersion("1.68")
            }
        }
    }
}