-- This premake5 script builds the Rive C++ Runtime and its dependencies.
-- It is invoked by CMakeLists.txt in the same directory.

-- RIVE_RUNTIME_DIR should be provided via the environment by CMake

local path = require('path')
local rive_runtime_dir = os.getenv('RIVE_RUNTIME_DIR')

-- Build the Rive Renderer
dofile(path.join(rive_runtime_dir, 'renderer/premake5_pls_renderer.lua'))
-- Build the Rive C++ Runtime
dofile(path.join(rive_runtime_dir, 'premake5_v2.lua'))

-- Consolidate all the libraries into one location.
-- We don't actually link against librive_cpp_runtime.a, but we use it to build the others.
project('rive_cpp_runtime')
do
    kind('StaticLib')
    links({
        'rive',
        'rive_harfbuzz',
        'rive_sheenbidi',
        'rive_yoga',
        'miniaudio',
        'luau_vm',
    })
end

-- rive-runtime's build scripts take the host's architecture for granted on
-- Windows, so an arm64 build has to name its target itself.
workspace('rive')
filter({ 'system:windows', 'options:arch=arm64' })
do
    buildoptions({ '--target=aarch64-pc-windows-msvc' })
end
filter({})

-- Without link-time optimization Premake archives with plain `ar`, which on
-- Windows is MinGW's and cannot index the arm64 objects clang produces: the
-- linker then finds none of their symbols. LLVM's own archiver can.
if os.target() == 'windows' then
    premake.tools.clang.tools.ar = 'llvm-ar'
end
