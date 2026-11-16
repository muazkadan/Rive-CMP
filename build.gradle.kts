import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.dokka) apply false
}

/** How each desktop OS is written in prose, and the extension of its shared libraries. */
val desktopOperatingSystems = mapOf(
    "macos" to ("macOS" to "dylib"),
    "linux" to ("Linux" to "so"),
    "windows" to ("Windows" to "dll"),
)

// Each runtime-<os>-<arch> module ships the prebuilt JNI bridge for one desktop platform as a
// resource (see native/rive-desktop) and publishes it as rive-cmp-runtime-<os>-<arch>. They differ
// only in the platform, so they have no build file of their own and are all configured here.
subprojects {
    if (!name.startsWith("runtime-")) return@subprojects

    val (os, arch) = name.removePrefix("runtime-").split("-")
    val (osName, libraryExtension) = desktopOperatingSystems.getValue(os)
    val platform = "$osName $arch"
    val library = "librive-desktop$arch.$libraryExtension"

    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")

    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // The notice for the third-party components linked into the binary, which are the same on
    // every platform
    tasks.named<ProcessResources>("processResources") {
        from(rootProject.file("native/rive-desktop/NOTICE")) {
            into("META-INF")
            filter<ReplaceTokens>("tokens" to mapOf("platform" to platform, "library" to library))
        }
    }

    extensions.configure<MavenPublishBaseExtension> {
        publishToMavenCentral()

        signAllPublications()

        // groupId and version come from GROUP / VERSION_NAME in gradle.properties
        coordinates(artifactId = "rive-cmp-${project.name}")

        pom {
            name = "Rive CMP Desktop Runtime ($platform)"
            description =
                "Native rive-runtime JNI binary for Rive CMP's JVM/Desktop target on $platform. " +
                    "Pulled in automatically as a runtime dependency of dev.muazkadan:rive-cmp - not intended for direct use."
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
}
