#!/bin/bash

# Builds the Skia static library the JNI bridge links against, for Linux and
# Windows. rive-runtime ships a script that does this for macOS
# (skia/dependencies/make_skia_macos.sh) but none for these two, so this one
# checks out the same Skia and builds it with the feature set of the macOS
# script, minus the macOS backends.
#
#   make_skia.sh <x64|arm64>
#
# The library lands where CMakeLists.txt looks for it:
#   submodules/rive-runtime/skia/dependencies/skia/out/<linux|windows>/<arch>/
#
# Needs git, curl, unzip, python3, ninja and clang. On Windows, run it from Git
# Bash; it uses the LLVM in C:\Program Files\LLVM unless CLANG_WIN names
# another one.

set -euo pipefail

ARCH="${1:-}"
case "$ARCH" in
    x64 | arm64) ;;
    *)
        echo "usage: make_skia.sh <x64|arm64>" >&2
        exit 1
        ;;
esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RIVE_RUNTIME_DIR="${RIVE_RUNTIME_DIR:-$SCRIPT_DIR/../../submodules/rive-runtime}"

SKIA_REPO="https://github.com/rive-app/skia"
# The tip of its "rive" branch, which is what make_skia_macos.sh builds
SKIA_COMMIT="355e333200d874be47b2edb80c1c3be166c3c046"

# Checks one commit of a repository out into a directory, without its history.
#   checkout <directory> <repository> <commit>
checkout() {
    [ -d "$1/.git" ] || git init -q "$1"
    git -C "$1" fetch -q --depth 1 "$2" "$3"
    git -C "$1" checkout -q FETCH_HEAD
}

case "$(uname -s)" in
    Linux*)
        OS=linux
        case "$(uname -m)" in
            x86_64) GN_HOST=linux-amd64 ;;
            aarch64) GN_HOST=linux-arm64 ;;
            *)
                echo "No gn build for a $(uname -m) host" >&2
                exit 1
                ;;
        esac
        # Fonts and windowing are the embedding application's business: the
        # bridge only rasterizes paths and images into a caller's buffer.
        PLATFORM_ARGS='
            cc="clang"
            cxx="clang++"
            skia_use_x11=false
        '
        ;;
    MINGW* | MSYS*)
        OS=windows
        # Also what an arm64 host runs, emulated
        GN_HOST=windows-amd64
        CLANG_WIN="${CLANG_WIN:-C:/Program Files/LLVM}"
        # Skia's raster pipeline is only vectorized when built with clang.
        # Its build looks the clang version up as an x.y.z directory, which
        # LLVM stopped using, so name the directory for it.
        # rive_use_picture builds Skia's picture recorder: the trimmed Skia
        # still refers to it from code nothing calls, which other linkers drop
        # but this one wants resolved.
        PLATFORM_ARGS='
            clang_win="'"$CLANG_WIN"'"
            clang_win_version="'"$(ls "$CLANG_WIN/lib/clang" | sort -V | tail -n 1)"'"
            skia_enable_fontmgr_win=false
            skia_enable_fontmgr_win_gdi=false
            skia_use_xps=false
            rive_use_picture=true
        '
        ;;
    *)
        echo "make_skia.sh builds Skia for Linux and Windows. On macOS use" >&2
        echo "$RIVE_RUNTIME_DIR/skia/dependencies/make_skia_macos.sh" >&2
        exit 1
        ;;
esac

cd "$RIVE_RUNTIME_DIR/skia/dependencies"
checkout skia "$SKIA_REPO" "$SKIA_COMMIT"
cd skia

# Skia's own tools/git-sync-deps fetches every optional dependency, with its
# whole history. The library is built from these alone, at the revisions Skia's
# DEPS file pins.
for DEPENDENCY in libgifcodec libjpeg-turbo libpng libwebp zlib; do
    PIN="$(sed -n -E "s|.*\"third_party/externals/$DEPENDENCY\" *: *\"([^\"]+)\".*|\1|p" DEPS)"
    checkout "third_party/externals/$DEPENDENCY" "${PIN%@*}" "${PIN#*@}"
done

# gn, at the revision Skia's bin/fetch-gn pins. That script does not know arm64
# Linux hosts, although there is a build for them.
GN_REVISION="$(sed -n -E "s/^ *rev = '([0-9a-f]+)'.*/\1/p" bin/fetch-gn)"
curl -fsSL -o gn.zip \
    "https://chrome-infra-packages.appspot.com/dl/gn/gn/$GN_HOST/+/git_revision:$GN_REVISION"
unzip -o -q gn.zip -x '.cipdpkg/*' -d bin
rm gn.zip
chmod +x bin/gn*

OUT="out/$OS/$ARCH"

# The defines are the ones CMakeLists.txt gives the bridge: they change the
# layout of Skia's classes, so both sides must be compiled with the same set.
# The rest is SHARED_ARGS of make_skia_macos.sh, without Metal and code signing.
bin/gn gen "$OUT" --args='
    target_cpu="'"$ARCH"'"
    extra_cflags=[
        "-DSK_DISABLE_SKPICTURE",
        "-DSK_DISABLE_TEXT",
        "-DSK_DISABLE_LEGACY_SHADERCONTEXT",
        "-DSK_DISABLE_LOWP_RASTER_PIPELINE",
        "-DSK_FORCE_RASTER_PIPELINE_BLITTER",
        "-DSK_DISABLE_AAA",
        "-DSK_DISABLE_EFFECT_DESERIALIZATION"
    ]
    '"$PLATFORM_ARGS"'
    is_official_build=true
    skia_use_freetype=false
    skia_use_metal=false
    skia_use_zlib=true
    skia_enable_gpu=true
    skia_use_libpng_encode=true
    skia_use_libpng_decode=true

    skia_use_angle=false
    skia_use_dng_sdk=false
    skia_use_egl=false
    skia_use_expat=false
    skia_use_fontconfig=false
    skia_use_system_freetype2=false
    skia_use_icu=false
    skia_use_libheif=false
    skia_use_system_libpng=false
    skia_use_system_libjpeg_turbo=false
    skia_use_libjpeg_turbo_encode=false
    skia_use_libjpeg_turbo_decode=true
    skia_use_libwebp_encode=false
    skia_use_libwebp_decode=true
    skia_use_system_libwebp=false
    skia_use_lua=false
    skia_use_piex=false
    skia_use_vulkan=false
    skia_use_gl=false
    skia_use_system_zlib=false
    skia_enable_fontmgr_empty=false
    skia_enable_spirv_validation=false
    skia_enable_pdf=false
    skia_enable_skottie=false
    skia_enable_tools=false
'

# Only the library itself: the default target also builds Skia's optional
# modules, which need system libraries the bridge has no use for.
ninja -C "$OUT" skia

ls -l "$OUT"/*skia.*
