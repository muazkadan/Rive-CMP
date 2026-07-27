plugins {
    `java-library`
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "dev.muazkadan"
version = "0.4.0"

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "rive-cmp-runtime-macos-arm64", version.toString())

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
