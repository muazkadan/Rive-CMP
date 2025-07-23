#!/bin/bash

set -ex

# Script to build Rive runtime libraries for Kotlin Multiplatform iOS targets
# This builds for iosX64(), iosArm64(), iosSimulatorArm64()

path=$(readlink -f "${BASH_SOURCE:-$0}")
DEV_SCRIPT_DIR=$(dirname $path)

if [ -d "$DEV_SCRIPT_DIR/../submodules/rive-runtime" ]; then
    export RIVE_RUNTIME_DIR="$DEV_SCRIPT_DIR/../submodules/rive-runtime"
else
    export RIVE_RUNTIME_DIR="$DEV_SCRIPT_DIR/../../runtime"
fi

export RIVE_PLS_DIR="$RIVE_RUNTIME_DIR/renderer"

# Set default premake args for KMP usage
if [ -z "${RIVE_PREMAKE_ARGS+null_detector_string}" ]; then
    RIVE_PREMAKE_ARGS="--with_rive_layout --with_rive_text --with_rive_audio=system"
fi

CONFIG="${1:-release}"  # Default to release build

if [ "$CONFIG" != "debug" ] && [ "$CONFIG" != "release" ]; then
    echo "Usage: $0 [debug|release]"
    exit 1
fi

echo "Building Rive runtime for Kotlin Multiplatform iOS targets ($CONFIG)..."

# Create output directories
rm -rf $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs
mkdir -p $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG

# Function to build for a specific architecture and variant
build_for_target() {
    local arch=$1
    local variant=$2
    local suffix=$3
    
    echo "Building for $arch with variant $variant..."
    
    # Build the main rive runtime
    pushd $RIVE_RUNTIME_DIR
    RIVE_PREMAKE_ARGS="$RIVE_PREMAKE_ARGS" ./build/build_rive.sh --file=premake5_v2.lua ios $CONFIG $arch ${variant:+$variant} clean
    
    # Determine output directory based on variant
    if [ "$variant" = "emulator" ]; then
        OUT_DIR="out/ios_${arch}_${CONFIG}"
    else
        OUT_DIR="out/ios_${arch}_${CONFIG}"
    fi
    
    # Copy main libraries
    cp $OUT_DIR/librive.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_${suffix}.a
    cp $OUT_DIR/librive_harfbuzz.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_harfbuzz_${suffix}.a
    cp $OUT_DIR/librive_yoga.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_yoga_${suffix}.a
    cp $OUT_DIR/librive_sheenbidi.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_sheenbidi_${suffix}.a
    cp $OUT_DIR/libminiaudio.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/libminiaudio_${suffix}.a
    popd
    
    # Build CG renderer
    pushd $RIVE_RUNTIME_DIR/cg_renderer
    export PATH="$RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/:$PATH"
    export PREMAKE_PATH="$RIVE_RUNTIME_DIR/build"
    if [ "$variant" = "emulator" ]; then
        $RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/premake5 --config=$CONFIG --out=out/iphonesimulator_${arch}_$CONFIG --arch=$arch --scripts=$RIVE_RUNTIME_DIR/build --os=ios --variant=emulator gmake2
        make -C out/iphonesimulator_${arch}_$CONFIG clean
        make -C out/iphonesimulator_${arch}_$CONFIG -j12 rive_cg_renderer
        cp out/iphonesimulator_${arch}_$CONFIG/librive_cg_renderer.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_cg_renderer_${suffix}.a
    else
        $RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/premake5 --config=$CONFIG --out=out/iphoneos_${arch}_$CONFIG --arch=$arch --scripts=$RIVE_RUNTIME_DIR/build --os=ios gmake2
        make -C out/iphoneos_${arch}_$CONFIG clean
        make -C out/iphoneos_${arch}_$CONFIG -j12 rive_cg_renderer
        cp out/iphoneos_${arch}_$CONFIG/librive_cg_renderer.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_cg_renderer_${suffix}.a
    fi
    popd
    
    # Build PLS renderer
    pushd $RIVE_PLS_DIR
    if [ "$variant" = "emulator" ]; then
        $RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/premake5 --config=$CONFIG --out=out/iphonesimulator_${arch}_$CONFIG --arch=$arch --scripts=$RIVE_RUNTIME_DIR/build --file=premake5_pls_renderer.lua --os=ios --variant=emulator gmake2
        make -C out/iphonesimulator_${arch}_$CONFIG clean
        make -C out/iphonesimulator_${arch}_$CONFIG -j12 rive_pls_renderer
        cp out/iphonesimulator_${arch}_$CONFIG/librive_pls_renderer.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_pls_renderer_${suffix}.a
    else
        $RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/premake5 --config=$CONFIG --out=out/iphoneos_${arch}_$CONFIG --arch=$arch --scripts=$RIVE_RUNTIME_DIR/build --file=premake5_pls_renderer.lua --os=ios gmake2
        make -C out/iphoneos_${arch}_$CONFIG clean
        make -C out/iphoneos_${arch}_$CONFIG -j12 rive_pls_renderer
        cp out/iphoneos_${arch}_$CONFIG/librive_pls_renderer.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_pls_renderer_${suffix}.a
    fi
    popd
    
    # Build decoders
    pushd $RIVE_RUNTIME_DIR/decoders
    if [ "$variant" = "emulator" ]; then
        $RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/premake5 --file=premake5_v2.lua --config=$CONFIG --out=out/iphonesimulator_${arch}_$CONFIG --arch=$arch --scripts=$RIVE_RUNTIME_DIR/build --os=ios --variant=emulator --no_rive_jpeg --no_rive_png --no_rive_webp gmake2
        make -C out/iphonesimulator_${arch}_$CONFIG clean
        make -C out/iphonesimulator_${arch}_$CONFIG -j12 rive_decoders
        cp out/iphonesimulator_${arch}_$CONFIG/librive_decoders.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_decoders_${suffix}.a
    else
        $RIVE_RUNTIME_DIR/build/dependencies/premake-core/bin/release/premake5 --file=premake5_v2.lua --config=$CONFIG --out=out/iphoneos_${arch}_$CONFIG --arch=$arch --scripts=$RIVE_RUNTIME_DIR/build --os=ios --no_rive_jpeg --no_rive_png --no_rive_webp gmake2
        make -C out/iphoneos_${arch}_$CONFIG clean
        make -C out/iphoneos_${arch}_$CONFIG -j12 rive_decoders
        cp out/iphoneos_${arch}_$CONFIG/librive_decoders.a $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/libs/$CONFIG/librive_decoders_${suffix}.a
    fi
    popd
}

# Build for each target
build_for_target "arm64" "" "iosarm64"                    # iosArm64()
build_for_target "x64" "emulator" "iosx64"               # iosX64() 
build_for_target "arm64" "emulator" "iossimulatorarm64"  # iosSimulatorArm64()

# Copy headers
echo "Copying headers..."
rm -rf $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/headers
mkdir -p $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/headers

cp -r $RIVE_RUNTIME_DIR/include/* $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/headers/
cp -r $RIVE_RUNTIME_DIR/cg_renderer/include/* $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/headers/
cp -r $RIVE_PLS_DIR/include/* $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/headers/
cp -r $RIVE_RUNTIME_DIR/decoders/include/* $DEV_SCRIPT_DIR/../library/src/nativeInterop/cinterop/headers/

echo "Build completed successfully!"
echo "Libraries are available in: library/src/nativeInterop/cinterop/libs/$CONFIG/"
echo "Headers are available in: library/src/nativeInterop/cinterop/headers/"