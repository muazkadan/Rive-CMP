plugins {
    `java-library`
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    // groupId and version come from GROUP / VERSION_NAME in gradle.properties
    coordinates(artifactId = "rive-cmp-runtime-macos-arm64")

    pom {
        name = "Rive CMP Desktop Runtime (macOS arm64)"
        description =
            "Native rive-runtime JNI binary for Rive CMP's JVM/Desktop target on macOS arm64 (Apple Silicon). " +
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
