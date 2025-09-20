plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
}

// F-Droid Build Setup - Access from root project
val fdroidBuild = rootProject.extra["fdroidBuild"] as String
val appBuildType = rootProject.extra["appBuildType"] as String

android {
    namespace = "com.manimarank.spell4wiki"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.manimarank.spell4wiki"
        vectorDrawables.useSupportLibrary = true
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionName = "3.1"
        versionCode = 19
        multiDexEnabled = true
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
        viewBinding = true
        buildConfig = true
    }

    bundle {
        language {
            // Specifies that the app bundle should not support
            // configuration APKs for language resources. These
            // resources are instead packaged with each base and
            // dynamic feature APK.
            enableSplit = false
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)

    // UI related libraries - using bundles
    implementation(libs.bundles.androidx.ui)

    // Architecture Components - using bundles
    implementation(libs.bundles.androidx.architecture)

    // Room Database - using bundles
    implementation(libs.bundles.androidx.room)
    kapt(libs.androidx.room.compiler)

    // Networking - using bundles
    implementation(libs.bundles.networking)

    // Coroutines - using bundles
    implementation(libs.bundles.coroutines)

    // Animation & UI - using bundles (temporarily disabled for build testing)
    // implementation(libs.bundles.animation.ui)
    implementation(libs.lottie)
    implementation(libs.material.tap.target.prompt)
    // implementation(libs.loading.button.android) // Temporarily disabled - repository issue

    // Audio Processing - FFmpeg Kit
    // Download aar file - https://artifactory.appodeal.com/appodeal-public/com/arthenica/mobile-ffmpeg-min/4.4.LTS/
    // https://github.com/AliAkhgar/ffmpeg-kit-16KB/releases
    // https://github.com/arthenica/ffmpeg-kit/issues/1012
    implementation(files("libs/ffmpeg-kit-lts-6.0-ndk-r25-16k.aar"))
    implementation("com.arthenica:smart-exception-java:0.2.1") // Added for FFmpeg Kit compatibility refer - https://medium.com/deemaze-software/android-building-the-archived-ffmpegkit-878db187cc2c

    // Image Loading
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Utilities
    implementation(libs.annotations)
    implementation(libs.crashreporter)

    // Testing - using bundles
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// F-Droid build: Excluded proprietary plugins, sdk libraries
if (!appBuildType.contains(fdroidBuild)) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
    
    dependencies {
        implementation(platform(libs.firebase.bom))
        implementation(libs.bundles.firebase)
    }
}
