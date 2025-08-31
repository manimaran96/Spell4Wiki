// Top-level build file where you can add configuration options common to all sub-projects/modules.

// F-Droid Build Setup
extra["fdroidBuild"] = "fdroid-build"
extra["playStoreBuild"] = "playstore-build"
extra["appBuildType"] = extra["fdroidBuild"] // Default build type is fdroid-build

val fdroidBuild = extra["fdroidBuild"] as String
val appBuildType = extra["appBuildType"] as String

println(" --------------------")
println("appBuildType: $appBuildType")
println(" --------------------")

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}





allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Keep mavenLocal() for local development if needed
        mavenLocal()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
