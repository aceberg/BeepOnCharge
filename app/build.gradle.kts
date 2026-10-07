import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.github.aceberg.beeponcharge"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.aceberg.beeponcharge"
        minSdk = 28
        targetSdk = 36

        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(
                keystoreProperties["storeFile"] as String
            )
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        )
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
}

tasks.register("renameReleaseApk") {
    dependsOn("assembleRelease")

    doLast {
        val dir = layout.buildDirectory
            .dir("outputs/apk/release")
            .get()
            .asFile

        val apk = dir.resolve("app-release.apk")
        val renamed = dir.resolve(
            "BeepOnCharge-${android.defaultConfig.versionName}-release.apk"
        )

        if (apk.exists()) {
            apk.renameTo(renamed)
        }
    }
}