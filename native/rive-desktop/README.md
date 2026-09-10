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

## The `submodules/rive-runtime` pin

The submodule is pinned to an exact commit:

```
073942e9261faa2f886fea733505751a131b6a1f
```

`.gitmodules` deliberately declares no `branch`, so `git submodule update --remote`
cannot silently move the pin. Changing which upstream commit we build against should
be a deliberate, reviewable commit that updates the gitlink and the checksum below.

## Provenance of the shipped binary

The prebuilt `runtime-macos-arm64/src/main/resources/librive-desktoparm64.dylib`
currently in this repository has this fingerprint:

```
sha256  baf660c9323d08239e464278cbcec2ffa4bc5129658473a5f8477f3bb5a2aab3
size    9191704 bytes
```

> [!WARNING]
> **This binary predates the pin above and is not reproducible from this repository.**
> It was built before the exact `rive-runtime` commit was recorded, so rebuilding from
> the pinned commit may produce a binary that behaves slightly differently. The
> checksum here identifies *what we currently ship*, not that it matches this source.
>
> To close this gap, run the **Build native desktop runtime** workflow
> (`.github/workflows/build-native-desktop.yml`), which builds the dylib from the
> pinned commit on a clean macOS arm64 runner and runs the JNI smoke test against it.
> Replace the committed binary with that artifact and update the checksum above; the
> warning can then be removed.

Whenever the binary is replaced, update the checksum in this file in the same commit.
Verify a copy with:

```sh
shasum -a 256 runtime-macos-arm64/src/main/resources/librive-desktoparm64.dylib
```
