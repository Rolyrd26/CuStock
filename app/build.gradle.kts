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
        create("release") {
            val releaseKeystore = rootProject.file("custock-release.p12")
            if (releaseKeystore.exists()) {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = "custock"
                keyPassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                storeType = "PKCS12"
            } else {
                logger.lifecycle("No se encontró custock-release.p12; la firma de release quedará deshabilitada.")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            if (rootProject.file("custock-release.p12").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    lint {
        abortOnError = false
    }
}
