# rive-desktop (JNI bridge)

A CMake project that builds `librive-desktop<arch>.{dylib,so,dll}`, a JNI shared
library bridging Kotlin/JVM to the C++ [rive-runtime](https://github.com/rive-app/rive-runtime),
rendering via Skia's CPU rasterizer into a caller-supplied pixel buffer.

This is **not** part of the default Gradle build for `:library` — configuring it
triggers a full source build of rive-runtime, Skia, and its other native
dependencies (minutes, requires Ninja, Premake, and a prebuilt Skia checkout at a
specific path). The `:library:jvmMain` target instead uses prebuilt binaries, one per
platform, each shipped as a resource of its `:runtime-<os>-<arch>` module
(`runtime-<os>-<arch>/src/main/resources/`) and published as
`dev.muazkadan:rive-cmp-runtime-<os>-<arch>`. Build this yourself only to modify the
bridge itself or to move the `rive-runtime` pin.

## Platforms

| Platform      | Library                      | Needs at run time                 |
|---------------|------------------------------|-----------------------------------|
| macOS arm64   | `librive-desktoparm64.dylib` | macOS 11                          |
| macOS x64     | `librive-desktopx64.dylib`   | macOS 11                          |
| Linux x64     | `librive-desktopx64.so`      | glibc 2.35 (`libc` and `libm`)    |
| Linux arm64   | `librive-desktoparm64.so`    | glibc 2.35 (`libc` and `libm`)    |
| Windows x64   | `librive-desktopx64.dll`     | nothing beyond `kernel32.dll`     |
| Windows arm64 | `librive-desktoparm64.dll`   | nothing beyond `kernel32.dll`     |

The Linux libraries are built on Ubuntu 22.04 and so need its glibc or a newer one; the C++
runtime is linked statically. The Windows libraries link the C runtime statically too.

## Building

The [Build native desktop runtime](../../.github/workflows/build-native-desktop.yml) workflow
builds every platform from a clean checkout, so its steps are the reference for what follows.

All of it is run from the repository root. Skia first, for the architecture you are building
(`arm64` or `x64`):

```sh
git submodule update --init --recursive submodules/rive-runtime

# macOS, after the workflow's "Patch the Skia build flags" step
(cd submodules/rive-runtime/skia/dependencies && ./make_skia_macos.sh arm64)

# Linux, and Windows from Git Bash
native/rive-desktop/make_skia.sh x64
```

Then the bridge, which also builds the Rive runtime's static libs:

```sh
export JAVA_HOME=<path to a JDK>
cmake -S native/rive-desktop -B native/rive-desktop/build -DCMAKE_BUILD_TYPE=Release
cmake --build native/rive-desktop/build
```

On Linux and Windows add `-G Ninja -DCMAKE_CXX_COMPILER=clang++`: Skia and the Rive runtime are
built with clang there, and so is the bridge. A Mac builds for its other architecture with
`-DCMAKE_OSX_ARCHITECTURES=arm64` or `x86_64`. The workflow's "Configure" step has the arguments
for cross-compiling to Windows arm64.

Produces `native/rive-desktop/build/librive-desktop<arch>.<ext>`.

What each platform needs installed:

- **macOS**: Xcode, and `ninja` and `premake` (`brew install ninja premake`)
- **Linux**: `clang`, `ninja`, `cmake`, `python3`, `git`, `curl`, `unzip`, `make`, a C compiler and the libuuid
  headers (`uuid-dev`), the last two for Premake, which `build_rive.sh` builds from source
- **Windows**: Visual Studio's C++ tools and Windows SDK, LLVM, `ninja`, `cmake`, `python`, `make`,
  Git Bash, and the SDK's `fxc.exe` on `PATH`

Known snags on macOS:

- `make_skia_macos.sh` invokes `python`, which no longer exists on macOS 26 (only `python3`). Put a
  `python` shim on `PATH` that execs `python3` - a symlink does not work, because `/usr/bin/python3`
  resolves the tool from `argv[0]`.
- The Skia this pins is from 2022 and its vendored zlib and libpng take Classic Mac OS code paths
  under `TARGET_OS_MAC`, so they fail against current SDKs (`fdopen` redefinition, missing `fp.h`).
  Two compiler flags keep them off those paths, `-Dfdopen=fdopen` and `-include math.h`, which the
  workflow adds to `make_skia_macos.sh`.
- Xcode 26 and later leave the Metal compiler, which the Rive renderer's shader build runs, to a
  separate download: `xcodebuild -downloadComponent MetalToolchain`.

## The `submodules/rive-runtime` pin

The submodule is pinned to an exact commit:

```
073942e9261faa2f886fea733505751a131b6a1f
```

`.gitmodules` deliberately declares no `branch`, so `git submodule update --remote`
cannot silently move the pin. Changing which upstream commit we build against should
be a deliberate, reviewable commit that updates the gitlink and the checksums below.

Skia is not a submodule: `make_skia_macos.sh` clones the tip of the `rive` branch of
[rive-app/skia](https://github.com/rive-app/skia), which has been
`355e333200d874be47b2edb80c1c3be166c3c046` since 2022, and `make_skia.sh` checks out that commit
by name.

## Provenance of the shipped binaries

The prebuilt libraries in `runtime-<os>-<arch>/src/main/resources/` are produced by the
**Build native desktop runtime** workflow
([`.github/workflows/build-native-desktop.yml`](../../.github/workflows/build-native-desktop.yml))
from the pinned `rive-runtime` commit above, on clean runners:

```
platform       sha256                                                            size
macos-arm64    6626ef756c6b33bd2bb66ee8e222eb82762d294bc547e72f6a40e78d18bbfbbb   9191704 bytes
macos-x64      aa906527551f19ce715e3077ac7caa11c9e3ee2eba8024b55130ad8e4f807657   9966264 bytes
linux-x64      52db44518eb81293d71411efe0067369a9dee4f823904d9c8afe16ded9b63a5b  11222936 bytes
linux-arm64    af8f3c4feaeacc1caf39e4cf9b8a095cad3fd2f86bdb79459037f8c2751dfb83  10955224 bytes
windows-x64    af9d86b9331b31fcbe3cee9e54b5e7d125e70458a9a94d1268f1f4b504b6a015   7837696 bytes
windows-arm64  74635576f704adb431c24fa5203880017e987b6d6b45958f421e91ba754a5702   6830080 bytes

built          GitHub Actions run 43907161302 (angelix/Rive-CMP)
```

The Linux and macOS x64 libraries came out byte-identical in earlier runs of the workflow from the
same sources; the Windows ones did except for the link time in their header, which they no longer
carry. The macOS arm64 library is cross-compiled on an Intel runner.

The macOS libraries are built with the flags of the build that first worked: the pinned
runtime's `make_skia_macos.sh` adds `-flto=full`, `-fembed-bitcode` and `-DRIVE_OPTIMIZED` to
`SHARED_EXTRA_CFLAGS`, and a library built with them segfaulted at `SkCanvas::save()` on first
render. `-flto=full` leaves LLVM bitcode in Skia's archives while this bridge is compiled without
LTO. The workflow strips those three flags before building; see
[#160](https://github.com/muazkadan/Rive-CMP/issues/160) for the full history.

Whenever a binary is replaced, update its checksum in this file in the same
commit, run `./gradlew :library:jvmTest` on that platform, and **run the desktop sample**. The JNI
smoke test alone passes on a binary that crashes on first render, which is why the
JVM tests also render an artboard headlessly
(`CustomRiveAnimationLayoutTest.drawsTheArtboard`); the sample remains the final
check.

Verify a copy with:

```sh
shasum -a 256 runtime-*/src/main/resources/librive-desktop*
```
