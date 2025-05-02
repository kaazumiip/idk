pluginManagement {
    repositories {
        google()  // Add Google repository
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()  // Add Google repository
        mavenCentral()
    }
}

rootProject.name = "schedule_application"
include(":app")
