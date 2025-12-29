plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.google.gms.googleServices)
}

android {
    namespace = "com.example.rukigaapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rukigaapp"
        minSdk = 24
        targetSdk = 35
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
        dataBinding = true
        viewBinding {enable = true }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Room
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

// Existing dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.runtime)

    // Import the Firebase BoM (Bill of Materials)
    // This allows you to manage all Firebase library versions with one BoM version
    implementation(platform(libs.firebase.bom))

    // Firebase Authentication
    // Note: Version is omitted because it is controlled by the BoM above
    implementation(libs.firebase.auth.ktx)

    // Google Play Services for Sign-In
    // Required for the "Sign in with Google" functionality
    implementation(libs.google.auth)
}