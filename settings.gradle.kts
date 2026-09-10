pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "rivecmp"
include(":library")
include(":runtime-macos-arm64")
include(":sample")
include(":androidSample")
