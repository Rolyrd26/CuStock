plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.custock.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.custock.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("stable") {
            storeFile = rootProject.file("custock-release.p12")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "custock"
            keyPassword = System.getenv("KEYSTORE_PASSWORD")
            storeType = "PKCS12"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("stable")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
