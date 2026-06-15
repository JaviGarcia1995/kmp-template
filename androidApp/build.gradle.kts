import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.Properties
import javax.inject.Inject

abstract class CopySharedComposeResources : DefaultTask() {
    @get:InputDirectory
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun copyResources() {
        fileSystemOperations.sync {
            from(inputDirectory)
            into(outputDirectory)
        }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}

fun signingProperty(name: String): String =
    keystoreProperties.getProperty(name) ?: throw GradleException("Missing $name in keystore.properties")

val sharedComposeResources =
    project(":shared").layout.buildDirectory.dir(
        "generated/assets/copyAndroidMainComposeResourcesToAndroidAssets"
    )

val copySharedComposeResources = tasks.register<CopySharedComposeResources>(
    "copySharedComposeResources"
) {
    dependsOn(":shared:copyAndroidMainComposeResourcesToAndroidAssets")
    inputDirectory.set(sharedComposeResources)
    outputDirectory.set(layout.buildDirectory.dir("generated/sharedComposeResources"))
}

android {
    namespace = "com.example.kmptemplate.android"
    compileSdk = 36

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(signingProperty("storeFile"))
                storePassword = signingProperty("storePassword")
                keyAlias = signingProperty("keyAlias")
                keyPassword = signingProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.kmptemplate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isMinifyEnabled = true
            isShrinkResources = true
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
    
    buildFeatures {
        compose = true
    }
}

// AGP 9 consumes local Android-KMP projects as JARs, which do not carry Compose assets.
androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            copySharedComposeResources,
            CopySharedComposeResources::outputDirectory
        )
    }
}

dependencies {
    // Shared module
    implementation(project(":shared"))
    
    // Android specific dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Koin
    implementation(libs.koin.android)
}
