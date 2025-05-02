plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.schedule_application"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.schedule_application"
        minSdk = 21
        targetSdk = 34
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX & UI
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")




    // Firebase (using BoM for version management)
    implementation(platform("com.google.firebase:firebase-bom:31.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation ("com.google.firebase:firebase-firestore:24.3.0")

    // Networking & Parsing
//    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")


    // Testing
    testImplementation("junit:junit:4.13.2")

    // Legacy Calendar View (monitor this)
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
}
