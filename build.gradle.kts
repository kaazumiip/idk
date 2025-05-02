// Apply necessary plugins at the project level
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}

buildscript {
    dependencies {
        // Ensure you are using the correct classpath for the Gradle plugin version
        classpath("com.android.tools.build:gradle:7.4.0")
        classpath("com.google.gms:google-services:4.4.1")

    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

