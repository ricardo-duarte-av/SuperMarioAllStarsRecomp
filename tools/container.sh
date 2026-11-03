# Sourced by tools/make_linux.sh and tools/make_windows_zip.sh: picks Docker or Podman
# and the options a build container needs on this machine.
#   CONTAINER_ENGINE=podman|docker   force one (default: docker if it answers, else podman)
# Sets ENGINE (the command), RUN_OPTS (extra `run` options) and MOUNT_OPT (volume suffix).
pick_container_engine() {
    ENGINE="${CONTAINER_ENGINE:-}"
    if [ -z "$ENGINE" ]; then
        if command -v docker >/dev/null && docker info >/dev/null 2>&1; then ENGINE=docker
        elif command -v podman >/dev/null; then ENGINE=podman
        else echo "FAIL: neither Docker nor Podman is installed" >&2; return 1; fi
    fi
    RUN_OPTS=(); MOUNT_OPT=""
    # Rootless Podman/Docker (and Docker Desktop/Colima) map the container's root to you; a rootful
    # engine on Linux would leave root-owned files behind, so build as the calling user there.
    local rootless=false
    if [ "$ENGINE" = podman ]; then
        rootless=$(podman info -f '{{.Host.Security.Rootless}}' 2>/dev/null || echo false)
    elif docker info -f '{{.SecurityOptions}}' 2>/dev/null | grep -q rootless; then
        rootless=true
    fi
    if [ "$(uname -s)" = Linux ] && [ "$rootless" != true ]; then
        RUN_OPTS=(--user "$(id -u):$(id -g)" -e HOME=/tmp)
    fi
    # SELinux (Fedora and friends) blocks the container from the repo unless the mount is relabeled.
    if command -v selinuxenabled >/dev/null && selinuxenabled; then MOUNT_OPT=":z"; fi
    return 0
}
