plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.github.panpf.sketch.sample"
    compileSdk = libs.versions.app.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.github.panpf.sketch3.sample"

        minSdk = libs.versions.app.minSdk.get().toInt()
        targetSdk = libs.versions.app.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = "${libs.versions.app.versionName.get()}.${getGitVersion()}"

        vectorDrawables.useSupportLibrary = true    // Converting svg to png under version 21 is not allowed
    }

    val releaseSigningConfig = readReleaseSigningConfig()
    signingConfigs {
        if (releaseSigningConfig != null) {
            create("release") {
                storeFile = releaseSigningConfig.storeFile
                storePassword = releaseSigningConfig.storePassword
                keyAlias = releaseSigningConfig.keyAlias
                keyPassword = releaseSigningConfig.keyPassword
            }
        }
    }
    buildTypes {
        getByName("debug") {
            if (releaseSigningConfig != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            multiDexEnabled = true
        }

        if (releaseSigningConfig != null) {
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions.add("default")

    androidResources {
        noCompress("bmp")
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    // Set both the Java and Kotlin compilers to target Java 8.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs =
            freeCompilerArgs + "-P" + "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    implementation(project(":sketch"))
    implementation(project(":sketch-compose"))
    implementation(project(":sketch-extensions"))
    implementation(project(":sketch-gif-movie"))
    implementation(project(":sketch-gif-koral"))
    implementation(project(":sketch-okhttp"))
    implementation(project(":sketch-svg"))
    implementation(project(":sketch-video"))
    implementation(project(":sketch-video-ffmpeg"))
    implementation(project(":sketch-zoom"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.bundles.androidx.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.multidex)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.bundles.androidx.paging)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.palette)

    implementation(libs.google.material)
    implementation(libs.bundles.panpf.assemblyadapter4)
    implementation(libs.bundles.panpf.tools4a)
    implementation(libs.bundles.panpf.tools4j)
    implementation(libs.panpf.tools4k)
    implementation(libs.panpf.liveevent)
    implementation(libs.panpf.activitymonitor)
    implementation(libs.bundles.retrofit2)
    implementation(libs.tinypinyin)
    implementation(libs.okhttp3.logging)
    implementation(libs.mmkv)

    debugImplementation(libs.leakcanary)
}

fun getGitVersion(): String =
    Runtime.getRuntime().exec("git rev-parse --short HEAD").inputStream.use {
        it.bufferedReader().readText().trim()
    }

fun readReleaseSigningConfig(): ReleaseSigningConfig? {
    val localProperties = `java.util`.Properties().apply {
        project.file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()?.use { this@apply.load(it) }
    }
    val jksFile = project.file("release.jks")
    return if (
        localProperties.containsKey("signing.storePassword")
        && localProperties.containsKey("signing.keyAlias")
        && localProperties.containsKey("signing.keyPassword")
        && jksFile.exists()
    ) {
        println("hasReleaseSigningConfig: true")
        ReleaseSigningConfig(
            localProperties.getProperty("signing.storePassword"),
            localProperties.getProperty("signing.keyAlias"),
            localProperties.getProperty("signing.keyPassword"),
            jksFile
        )
    } else {
        println("hasReleaseSigningConfig: false")
        null
    }
}

class ReleaseSigningConfig(
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
    val storeFile: File,
)