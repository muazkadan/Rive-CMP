@file:OptIn(ExperimentalSpmForKmpFeature::class)

import io.github.frankois944.spmForKmp.utils.ExperimentalSpmForKmpFeature
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.spmForKmp)
    alias(libs.plugins.dokka)
}

group = "dev.muazkadan"
version = "0.0.6"
kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "RiveCMP"
            isStatic = true
        }
        it.compilations {
            val main by getting {
                cinterops.create("nativeIosShared")
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.rive.android)
            api(libs.androidx.startup)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "dev.muazkadan.rivecmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "rive-cmp", version.toString())

    pom {
        name = "Rive CMP"
        description =
            "A Compose Multiplatform wrapper library for integrating Rive animations, providing a unified API to use rive-android and rive-ios seamlessly across Android and iOS platforms."
        inceptionYear = "2025"
        url = "https://github.com/muazkadan/Rive-CMP"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "muazkadan"
                name = "Muaz KADAN"
                url = "https://muazkadan.dev/"
                email = "muaz.kadan@gmail.com"
            }
        }
        scm {
            url = "https://github.com/muazkadan/Rive-CMP"
            connection = "scm:git:git://github.com/muazkadan/Rive-CMP.git"
            developerConnection = "scm:git:ssh://github.com/muazkadan/Rive-CMP.git"
        }
    }
}

swiftPackageConfig {
    create("nativeIosShared") {
        minIos = "14.0"
        spmWorkingPath =
            "${projectDir.resolve("SPM")}" // change the Swift Package Manager working Dir
        exportedPackageSettings { includeProduct = listOf("RiveRuntime") }
        dependency {
            remotePackageVersion(
                url = URI("https://github.com/rive-app/rive-ios.git"),
                version = "6.10.0",
                products = {
                    add("RiveRuntime")
                },
            )
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
