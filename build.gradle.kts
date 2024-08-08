// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.androidLibrary) apply false
    `maven-publish`
}

buildscript {
    dependencies {
        classpath (libs.android.maven.gradle.plugin)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filesMatching("lint-resources.xml") {
        exclude()
    }
}