cmake_minimum_required(VERSION 3.18.1)

# The oldest macOS the library loads on, which is the one rive-runtime builds
# its static libs for. Left unset, it would be the macOS version of whatever
# SDK the library happened to be built with.
set(CMAKE_OSX_DEPLOYMENT_TARGET "11.0" CACHE STRING "Minimum macOS version")

project(rive-desktop VERSION 1.0.0 LANGUAGES CXX)

# Compile detail will be in rive-desktop/kotlin/.cxx/Debug/<hash>/<ABI>/compile_commands.json
#   e.g: kotlin/.cxx/Debug/4o1b5h48/arm64-v8a/compile_commands.json
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")
set(CMAKE_VERBOSE_MAKEFILE ON)

# Find all source files
file(GLOB SOURCES CONFIGURE_DEPENDS
        src/*.cpp
        src/bindings/*.cpp
        src/helpers/*.cpp
        src/models/*.cpp
)

# Create the rive-desktop library target with the given sources
add_library(rive-desktop SHARED ${SOURCES})

# List of static libraries
# These must match what the premake invocation below emits into
# out/<arch>_<config>. rive_pls_renderer is required even though this bridge
# renders through Skia's CPU rasterizer: with WITH_SCRIPTING=ON, librive.a's
# Luau shader bindings reference rive::gpu::RenderContext and RiveRenderer.
# librive_cpp_runtime.a is deliberately not listed - it is an aggregate archive
# whose members are these .a files, which ld rejects ("librive.a not a mach-o
# file"). Link the components instead.
set(static_libs
        rive
        rive_harfbuzz
        rive_sheenbidi
        rive_yoga
        rive_pls_renderer
)

# Enable C++17
target_compile_features(rive-desktop PUBLIC cxx_std_17)
target_compile_options(rive-desktop PRIVATE
        -Wall # Enable all warnings
        -fno-exceptions # Disable C++ exceptions
        -fno-rtti # Disable C++ RTTI
        $<$<CONFIG:Release>:-Oz>) # Optimize for size with -Oz in release
target_compile_definitions(rive-desktop PRIVATE
        RIVE_DESKTOP=
        YOGA_EXPORT=
        $<$<CONFIG:Debug>:DEBUG>)

# Skia is built by make_skia_macos.sh (macOS) or make_skia.sh (Linux, Windows)
# with these exact flags (SHARED_EXTRA_CFLAGS).
# They change SkCanvas/SkPaint object layout, so every TU that includes Skia
# headers MUST define the same set - otherwise construction and use disagree on
# member offsets and crash at runtime (see canvas_factory.cpp's SK_DISABLE_TEXT guard).
target_compile_definitions(rive-desktop PRIVATE
        SK_DISABLE_SKPICTURE
        SK_DISABLE_TEXT
        SK_DISABLE_LEGACY_SHADERCONTEXT
        SK_DISABLE_LOWP_RASTER_PIPELINE
        SK_FORCE_RASTER_PIPELINE_BLITTER
        SK_DISABLE_AAA
        SK_DISABLE_EFFECT_DESERIALIZATION)

# Skia's own build also defines these for everything that includes its headers
# (the skia_public config in its BUILD.gn), per platform.
if (CMAKE_SYSTEM_NAME STREQUAL "Linux")
    # Makes Skia's native 32-bit colour type BGRA on Linux
    target_compile_definitions(rive-desktop PRIVATE SK_R32_SHIFT=16)
endif ()

if (WIN32)
    # What rive-runtime's build defines for Windows code that includes its
    # headers
    target_compile_definitions(rive-desktop PRIVATE
            NOMINMAX
            _USE_MATH_DEFINES)
    # Skia and the Rive runtime both link the static C runtime (/MT)
    set_target_properties(rive-desktop PROPERTIES
            MSVC_RUNTIME_LIBRARY "MultiThreaded")
    # Stamp the library with a hash of its content instead of the link time,
    # so the same sources give the same file
    target_link_options(rive-desktop PRIVATE -Wl,/Brepro)
endif ()

# Configure for monorepo vs. rive-desktop repo
if (EXISTS "${PROJECT_SOURCE_DIR}/../../submodules/rive-runtime")
    # PROJECT_SOURCE_DIR is native/rive-desktop, so the repo root - which is where
    # submodules/rive-runtime lives - is two levels up, not one.
    set(PACKAGES_DIR "${PROJECT_SOURCE_DIR}/../..")
    set(RIVE_RUNTIME_DIR "${PACKAGES_DIR}/submodules/rive-runtime")
else ()
    set(PACKAGES_DIR "${PROJECT_SOURCE_DIR}/../../../../..")
    set(RIVE_RUNTIME_DIR "${PACKAGES_DIR}/runtime")
endif ()
# Pass the resolved runtime directory to the environment for Premake
set(ENV{RIVE_RUNTIME_DIR} "${RIVE_RUNTIME_DIR}")

# Add an option to remove miniaudio
# Passed from Gradle with -DWITH_RIVE_AUDIO=ON|OFF
option(WITH_RIVE_AUDIO "Enable Rive audio support" ON)
if (WITH_RIVE_AUDIO)
    # Compile flag picked up by JNI C++
    target_compile_definitions(rive-desktop PRIVATE
            WITH_RIVE_AUDIO
            MA_NO_RESOURCE_MANAGER)
    # Value for Premake --with_rive_audio
    set(RIVE_AUDIO_ARG "system")
    # Add to the list of static libraries
    list(APPEND static_libs miniaudio)
else ()
    # Value for Premake --with_rive_audio
    set(RIVE_AUDIO_ARG "disabled")
endif ()

# Add an option to remove scripting
# Passed from Gradle with -DWITH_SCRIPTING=ON|OFF
option(WITH_SCRIPTING "Enable Rive scripting support" ON)
if (WITH_SCRIPTING)
    # Flag for Premake
    set(RIVE_SCRIPTING_ARG "--with_rive_scripting")
    # Add to the list of static libraries
    list(APPEND static_libs luau_vm)
endif ()

# Add an option to enable ASAN
# Passed from Gradle with -DENABLE_ASAN=ON|OFF
option(ENABLE_ASAN "Enable Address Sanitizer" OFF)
if (ENABLE_ASAN)
    # Pass to Premake to enable ASAN on librive and its dependencies
    set(RIVE_ASAN_FLAG "--with-asan")

    # Add ASAN flags to rive-desktop as well
    target_compile_options(rive-desktop PRIVATE
            -fsanitize=address
            -fno-omit-frame-pointer)
    target_link_options(rive-desktop PRIVATE
            -fsanitize=address
            -Wl,--export-dynamic
            -Wl,--no-undefined)
endif ()

# Debug/release configuration
message("librive-desktop build type: ${CMAKE_BUILD_TYPE}")
#[[ CMake adds these flags depending on CMAKE_BUILD_TYPE
    1. Debug: `-O0 -g`
    2. Release: `-O3 -DNDEBUG`
    3. RelWithDebInfo: `-O2 -g -DNDEBUG`
    4. MinSizeRel: `-Os -DNDEBUG`
]]
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CONFIG "debug")
elseif (CMAKE_BUILD_TYPE STREQUAL "Release" OR
        CMAKE_BUILD_TYPE STREQUAL "RelWithDebInfo" OR
        CMAKE_BUILD_TYPE STREQUAL "MinSizeRel")
    set(CONFIG "release")
endif ()

# Translate CMake processor ABI to Rive architecture
# A Mac builds for its other architecture with CMAKE_OSX_ARCHITECTURES, which
# leaves CMAKE_SYSTEM_PROCESSOR naming the host.
if (APPLE AND CMAKE_OSX_ARCHITECTURES)
    set(TARGET_PROCESSOR "${CMAKE_OSX_ARCHITECTURES}")
else ()
    set(TARGET_PROCESSOR "${CMAKE_SYSTEM_PROCESSOR}")
endif ()
if (TARGET_PROCESSOR MATCHES "x86_64" OR TARGET_PROCESSOR MATCHES "AMD64")
    set(RIVE_ARCH "x64")
elseif (TARGET_PROCESSOR MATCHES "i[3-6]86")
    set(RIVE_ARCH "x86")
elseif (TARGET_PROCESSOR MATCHES "arm64" OR TARGET_PROCESSOR MATCHES "ARM64" OR TARGET_PROCESSOR MATCHES "aarch64")
    set(RIVE_ARCH "arm64")
elseif (TARGET_PROCESSOR MATCHES "arm")
    set(RIVE_ARCH "arm")
else ()
    message(FATAL_ERROR "Unsupported architecture: ${TARGET_PROCESSOR}")
endif ()

# The Kotlin loader looks for lib<name><arch>.<ext> on every platform, so keep
# the prefix where the toolchain would drop it (Windows).
set_target_properties(rive-desktop PROPERTIES
        PREFIX "lib"
        OUTPUT_NAME "rive-desktop${RIVE_ARCH}"
)

# The static libs go into a shared library, so outside macOS, where all code is
# position-independent, they have to be built as such. Link-time optimization
# is left to macOS: elsewhere it ties the static libs to one linker.
if (APPLE)
    set(RIVE_PLATFORM_ARGS "")
elseif (WIN32)
    set(RIVE_PLATFORM_ARGS --no-lto)
else ()
    set(RIVE_PLATFORM_ARGS --with-pic --no-lto)
endif ()

# Build the Rive C++ runtime and its dependencies as static .a libs
execute_process(
        COMMAND bash ${RIVE_RUNTIME_DIR}/build/build_rive.sh --file=premake5_cpp_runtime.lua ninja ${CONFIG} ${RIVE_ARCH} --no-rive-decoders --with_rive_audio=${RIVE_AUDIO_ARG} ${RIVE_SCRIPTING_ARG} ${RIVE_ASAN_FLAG} ${RIVE_PLATFORM_ARGS} -- rive_cpp_runtime rive_pls_renderer
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        RESULT_VARIABLE SCRIPT_RESULT
        OUTPUT_VARIABLE SCRIPT_OUTPUT
        ECHO_OUTPUT_VARIABLE
)
if (NOT SCRIPT_RESULT EQUAL "0")
    message(FATAL_ERROR "Script returned with error: '${SCRIPT_OUTPUT}' - '${SCRIPT_RESULT}'")
endif ()

# Add header files
target_include_directories(rive-desktop PUBLIC
        ${RIVE_RUNTIME_DIR}/skia/dependencies/skia
        ${RIVE_RUNTIME_DIR}/skia/dependencies/skia/include/core
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${RIVE_RUNTIME_DIR}/include
        ${RIVE_RUNTIME_DIR}/renderer/include)

# Link each static library
foreach (X IN LISTS static_libs)
    set(LIB_TARGET "${X}-lib")
    add_library(${LIB_TARGET} STATIC IMPORTED)
    # Find the static lib outputs from Premake
    set_target_properties(${LIB_TARGET}
            PROPERTIES IMPORTED_LOCATION
            ${CMAKE_CURRENT_SOURCE_DIR}/out/${RIVE_ARCH}_${CONFIG}/${CMAKE_STATIC_LIBRARY_PREFIX}${X}${CMAKE_STATIC_LIBRARY_SUFFIX}
    )
    # Link
    target_link_libraries(rive-desktop ${LIB_TARGET})
endforeach ()

# Link Skia - the same checkout the include path points at, built by
# ${RIVE_RUNTIME_DIR}/skia/dependencies/make_skia_macos.sh <arch> on macOS and
# by make_skia.sh <arch> in this directory on Linux and Windows
if (APPLE)
    set(SKIA_OS "macosx")
elseif (WIN32)
    set(SKIA_OS "windows")
else ()
    set(SKIA_OS "linux")
endif ()
add_library(skia-lib STATIC IMPORTED)
set_target_properties(skia-lib PROPERTIES IMPORTED_LOCATION
        ${RIVE_RUNTIME_DIR}/skia/dependencies/skia/out/${SKIA_OS}/${RIVE_ARCH}/${CMAKE_STATIC_LIBRARY_PREFIX}skia${CMAKE_STATIC_LIBRARY_SUFFIX}
)
target_link_libraries(rive-desktop skia-lib)

if (APPLE)
    message(STATUS "Linking macOS CoreFoundation and CoreText frameworks")
    target_link_libraries(rive-desktop
            "-framework CoreFoundation"
            "-framework CoreGraphics"
            "-framework CoreText"
            "-framework Foundation"
            "-framework AppKit"
    )
elseif (NOT WIN32)
    target_link_options(rive-desktop PRIVATE
            # The static libs reference each other in both directions (librive's
            # scripting bindings use the renderer, which is built on librive),
            # which a linker that reads them once, in order, cannot resolve.
            # The group is closed below, after the last of them.
            -Wl,--start-group
            # Fail the build on a missing symbol rather than the first load
            -Wl,--no-undefined
            # Keep the static libs' symbols out of the dynamic symbol table, so
            # they can neither clash with nor be replaced by another library's
            -Wl,--exclude-libs,ALL
            -Wl,--gc-sections
            # Depend on glibc alone, not on the build machine's C++ runtime
            -static-libstdc++
            -static-libgcc)
    target_link_libraries(rive-desktop
            -Wl,--end-group
            dl
            pthread
            m)
endif ()

find_package(JNI REQUIRED)

# Add JNI include directories to your target
target_include_directories(rive-desktop PRIVATE ${JNI_INCLUDE_DIRS})
