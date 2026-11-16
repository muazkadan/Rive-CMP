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
// One module per desktop platform the JNI bridge is built for, configured in the root build file
include(":runtime-macos-arm64")
include(":runtime-macos-x64")
include(":runtime-linux-x64")
include(":runtime-linux-arm64")
include(":runtime-windows-x64")
include(":runtime-windows-arm64")
include(":sample")
include(":androidSample")
