@file:Suppress("UnstableApiUsage")

import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}

android {
    namespace = "de.salomax.currencies"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "de.salomax.currencies"
        minSdk = 26
        targetSdk = 35
        // SemVer
        versionName = "1.23.0"
        versionCode = 12300
        setProperty("archivesBaseName", "$applicationId-v$versionCode")
    }

    signingConfigs {
        create("release") {
            if (getSecret("KEYSTORE_FILE") != null) {
                storeFile = File(getSecret("KEYSTORE_FILE")!!)
                storePassword = getSecret("KEYSTORE_PASSWORD")
                keyAlias = getSecret("KEYSTORE_KEY_ALIAS")
                keyPassword = getSecret("KEYSTORE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " [DEBUG]"
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("play") {
            dimension = "version"
        }
        create("fdroid") {
            dimension = "version"
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        disable.add("MissingTranslation")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // kotlin
    implementation("androidx.core:core-ktx:1.16.0")
    // support libs
    val appCompatVersion = "1.7.1"
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$appCompatVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    val livecycleVersion = "2.9.1"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$livecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$livecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$livecycleVersion")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.window:window:1.4.0")
    implementation("com.google.android.material:material:1.12.0")
    // downloader
    val fuelVersion = "2.3.1"
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-android:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-moshi:$fuelVersion")
    val moshiVersion = "1.15.2"
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    // math (v5 releases use incompatible license to fdroid: noinspection GradleDependency)
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:4.4.3")
    // charts
    implementation("com.robinhood.spark:spark:1.2.0")
    // test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.18.0")
}

fun getSecret(key: String): String? {
    val secretsFile: File = rootProject.file("secrets.properties")
    return if (secretsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(secretsFile))
        props.getProperty(key)
    } else {
        null
    }
}

// versionCode <-> versionName /////////////////////////////////////////////////////////////////////

/**
 * Checks if versionCode and versionName match.
 * Needed because of F-Droid: both have to be hard-coded and can't be assigned dynamically.
 * So at least check during build for them to match.
 */
tasks.register("checkVersion") {
    doLast {
        val versionCode: Int? = android.defaultConfig.versionCode
        val correctVersionCode: Int = generateVersionCode(android.defaultConfig.versionName!!)
        if (versionCode != correctVersionCode) throw GradleException(
            "versionCode and versionName don't match: versionCode should be $correctVersionCode. Is $versionCode."
        )
    }
}
tasks.findByName("assemble")!!.dependsOn(tasks.findByName("checkVersion")!!)

/**
 * Checks if a fastlane changelog for the current version is present.
 */
tasks.register("checkFastlaneChangelog") {
    doLast {
        val versionCode: Int? = android.defaultConfig.versionCode
        val changelogFile: File =
            file("$rootDir/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt")
        if (!changelogFile.exists())
            throw GradleException(
                "Fastlane changelog missing: expecting file '$changelogFile'"
            )
    }
}
tasks.findByName("build")!!.dependsOn(tasks.findByName("checkFastlaneChangelog")!!)

/**
 * Generates a versionCode based on the given semVer String.
 *
 * @param semVer e.g. 1.3.1
 * @return e.g. 10301 (-> 1 03 01)
 */
fun generateVersionCode(semVer: String): Int {
    return semVer.split('.')
        .map { Integer.parseInt(it) }
        .reduce { sum, value -> sum * 100 + value }
}
