import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.spmForKmp)
}

group = "dev.muazkadan"
version = "0.0.1"
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
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

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

dependencies {
    debugImplementation(compose.uiTooling)
}
