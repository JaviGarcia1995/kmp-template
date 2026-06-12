// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android Plugins
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    
    // Kotlin Plugins
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    
    // KMP Plugins
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    
    // Other Plugins
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt) apply false
}
