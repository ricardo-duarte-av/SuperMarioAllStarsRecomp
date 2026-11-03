# Cross-compile the Windows .exe with MinGW-w64: Homebrew's on a Mac, or a Linux
# distribution's (tools/windows/Dockerfile). The ROM never leaves this machine,
# so Windows builds happen here, not in CI.
set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_SYSTEM_PROCESSOR x86_64)
set(CMAKE_C_COMPILER   x86_64-w64-mingw32-gcc)
set(CMAKE_CXX_COMPILER x86_64-w64-mingw32-g++)
set(CMAKE_RC_COMPILER  x86_64-w64-mingw32-windres)
foreach(_root /opt/homebrew/opt/mingw-w64/toolchain-x86_64 /usr/local/opt/mingw-w64/toolchain-x86_64
              /usr/x86_64-w64-mingw32/sys-root/mingw /usr/x86_64-w64-mingw32)
  if(EXISTS "${_root}")
    set(CMAKE_FIND_ROOT_PATH "${_root}")
    break()
  endif()
endforeach()
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE BOTH)
# Ship one .exe without the MinGW runtime DLLs beside it.
set(CMAKE_EXE_LINKER_FLAGS_INIT "-static -static-libgcc -static-libstdc++")
