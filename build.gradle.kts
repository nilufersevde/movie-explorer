// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // These are aliases from your version catalog (libs.versions.toml)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Firebase services (Google Services plugin)
    id("com.google.gms.google-services") version "4.4.1" apply false
}

allprojects {
    // Optional: configure global settings here if needed
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
