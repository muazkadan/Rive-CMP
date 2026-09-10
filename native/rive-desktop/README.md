# rive-desktop (JNI bridge)

A CMake project that builds `librive-desktop<arch>.{dylib,so,dll}`, a JNI shared
library bridging Kotlin/JVM to the C++ [rive-runtime](https://github.com/rive-app/rive-runtime),
rendering via Skia's CPU rasterizer into a caller-supplied pixel buffer.

This is **not** part of the default Gradle build for `:library` — configuring it
triggers a full source build of rive-runtime, Skia, and its other native
dependencies (minutes, requires Ninja, Premake, and a prebuilt Skia checkout at a
specific path). The `:library:jvmMain` target instead ships a prebuilt binary as a
resource (see `library/src/jvmMain/resources/`). Build this yourself only if you
need to target a platform/arch the prebuilt binary doesn't cover, or to modify the
bridge itself.

## Building

```sh
git submodule update --init --recursive submodules/rive-runtime
export JAVA_HOME=<path to a JDK>
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build
```

Produces `build/librive-desktop<arch>.<ext>` for the host OS/arch.

## Scope

Only macOS arm64 has been built and verified so far. The CMake logic has code
paths for macOS x64, Linux, and Windows, but they're unverified — building for
those requires their own toolchains and a prebuilt Skia checkout for that
platform. Contributions welcome.

## Note on the `submodules/rive-runtime` pin

The submodule currently tracks `rive-app/rive-runtime`'s `main` branch tip as of
when this was added. The exact upstream commit the shipped prebuilt binary was
originally built against wasn't recorded precisely enough to pin here — if you
rebuild from a different `rive-runtime` commit than the one used for the prebuilt
`.dylib`, your local build may produce a binary that behaves slightly differently
from the one currently bundled.
