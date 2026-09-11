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

Requires `ninja` and `premake` (`brew install ninja premake`).

Two known snags on recent macOS:

- `make_skia_macos.sh` invokes `python`, which no longer exists on macOS 26 (only `python3`). Put a
  `python` shim on `PATH` that execs `python3` - a symlink does not work, because `/usr/bin/python3`
  resolves the tool from `argv[0]`.
- The Skia this pins is from 2022 and its vendored zlib and libpng take Classic Mac OS code paths
  under `TARGET_OS_MAC`, so they fail against modern SDKs (`fdopen` redefinition, missing `fp.h`).
  It builds against the macOS 14.5 SDK; the
  [Build native desktop runtime](../../.github/workflows/build-native-desktop.yml) workflow uses
  `macos-14` for this reason.

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
is produced by the **Build native desktop runtime** workflow
([`.github/workflows/build-native-desktop.yml`](../../.github/workflows/build-native-desktop.yml))
from the pinned `rive-runtime` commit above, on a clean macOS arm64 runner:

```
sha256  1d6bcd505f64f4b733755484f6c5f69a8171a3589dfd7e2a1e91297f8f149409
size    9539800 bytes
built   GitHub Actions run 34628260905
```

The binary this replaced was built before the pin was recorded and could not be
reproduced from this repository. A rebuild from the pin initially segfaulted at
`SkCanvas::save()` on first render: the pinned runtime's `make_skia_macos.sh` adds
`-flto=full`, `-fembed-bitcode` and `-DRIVE_OPTIMIZED` to `SHARED_EXTRA_CFLAGS`,
which the original build never used. `-flto=full` leaves LLVM bitcode in Skia's
archives while this bridge is compiled without LTO. The workflow now strips those
three flags before building; see
[#160](https://github.com/muazkadan/Rive-CMP/issues/160) for the full history.

Whenever the binary is replaced, update the checksum in this file in the same
commit, and verify the result by **running the desktop sample** — the JNI smoke
test passes on a binary that crashes on first render, so it does not distinguish
a good build from a bad one.

Verify a copy with:

```sh
shasum -a 256 runtime-macos-arm64/src/main/resources/librive-desktoparm64.dylib
```
