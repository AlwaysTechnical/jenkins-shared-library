import org.devops.Docker

/**
 * Clean up Docker resources (containers, images, volumes, networks, build cache).
 *
 * Usage:
 *   dockerClean
 *   dockerClean containers: false, images: true, volumes: true
 *   dockerClean buildCache: true, pruneAll: true
 *
 * @param config  map with:
 *   - containers    : remove stopped containers (default true)
 *   - images        : remove dangling images (default true)
 *   - volumes       : remove unused volumes (default true)
 *   - networks      : remove unused networks (default true)
 *   - buildCache    : prune build cache (default false)
 *   - force         : skip confirmation prompt (default true)
 *   - pruneAll      : remove all unused images, not just dangling (default false)
 */
def call(Map config = [:]) {
  def docker = new Docker(this)
  docker.clean(config)
}