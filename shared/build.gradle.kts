import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.example.kmptemplate.shared"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget()
    
    iosArm64() {
        binaries.framework {
            baseName = "shared"
            isStatic = true
            binaryOption("bundleId", "com.example.kmptemplate.shared")
            linkerOpts("-lsqlite3")
        }
    }
    iosSimulatorArm64() {
        binaries.framework {
            baseName = "shared"
            isStatic = true
            binaryOption("bundleId", "com.example.kmptemplate.shared")
            linkerOpts("-lsqlite3")
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.koin.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.navigation.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.ios)
        }

    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.kmptemplate.database")
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    ignoreFailures = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "11"
    exclude("**/build/**", "**/generated/**")
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}

tasks.register<Detekt>("detektShared") {
    group = "verification"
    description = "Runs Detekt on shared commonMain source set."
    setSource(files("src/commonMain/kotlin"))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/generated/**")
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
    jvmTarget = "11"
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}
