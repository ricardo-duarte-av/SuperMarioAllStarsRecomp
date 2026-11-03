# Build machine for the Windows release when the host has no MinGW-w64 of its own
# (tools/make_windows_zip.sh starts it on Linux). Fedora's MinGW uses POSIX threads
# and ships the static runtime, like Homebrew's mingw-w64 on a Mac. Players need none of this.
FROM fedora:41
RUN dnf install -y --setopt=install_weak_deps=False \
        mingw64-gcc mingw64-gcc-c++ mingw64-winpthreads-static \
        cmake ninja-build git python3 zip file findutils \
    && dnf clean all
